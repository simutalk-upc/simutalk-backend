# CLAUDE.md

Guía para cualquier agente (o persona) que trabaje en este repositorio. Léela completa antes de cambiar código.

## Producto

**SimuTalk** es una API REST para una plataforma de preselección de talento
(curso 1ASI0705 Arquitectura de Aplicaciones Web, UPC, ciclo 202620).

1. Cada **empresa** publica una **vacante** y define sus propios **criterios de evaluación**, cada uno con un
   **peso relativo** (los pesos suman exactamente 100).
2. El sistema conduce una **entrevista asincrónica** al **postulante**.
3. Cada respuesta se **puntúa con NLP** contra los criterios de la vacante.
4. Se devuelve un **ranking** de postulantes donde **cada puntaje es trazable** hasta el fragmento textual de la
   respuesta que lo sustenta (evidencia). Un puntaje sin evidencia no se muestra.

## Stack

| Pieza | Versión / herramienta |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.5.x (web, data-jpa, security, validation, webflux para `WebClient`) |
| Persistencia | Spring Data JPA + Hibernate, MySQL 8 (`simutalk_db`) |
| Seguridad | Spring Security + JWT (jjwt 0.12.6) — *pendiente en el contexto `iam`* |
| Documentación | OpenAPI 3 con springdoc 2.7.0 (Swagger UI en `/swagger-ui.html`) |
| Utilidades | Lombok, ModelMapper 3.2.1 |
| Build | Maven (usar siempre `./mvnw`) |

Comandos:

```bash
./mvnw test                 # pruebas unitarias (no requieren base de datos)
./mvnw spring-boot:run      # levanta la API (requiere MySQL y DB_USERNAME / DB_PASSWORD)
```

## Arquitectura: DDD con bounded contexts

Paquete raíz `pe.upc.simutalk`. Cada bounded context es un paquete de primer nivel con **cuatro capas**:

| Capa | Paquete | Contenido |
|---|---|---|
| **Domain** | `domain/model/{aggregates,entities,commands,queries,valueobjects}`, `domain/services` | Agregados, entidades, value objects, commands, queries e interfaces de servicios. Aquí viven las reglas de negocio. Sin dependencias de web. |
| **Application** | `application/internal/{commandservices,queryservices,outboundservices}` | Implementaciones de los servicios de dominio: cargan el agregado, delegan en él y persisten. `outboundservices` son ACL hacia sistemas externos. |
| **Infrastructure** | `infrastructure/persistence/jpa/repositories` | Repositorios Spring Data JPA (y, en su momento, clientes HTTP a servicios externos). |
| **Interfaces** | `interfaces/rest/{resources,transform}` | Controladores REST, DTOs (`resources`) y ensambladores DTO ↔ command/entidad (`transform`). |

Flujo de una escritura: `Controller` → `*CommandFromResourceAssembler` → `Command` → `CommandService` →
agregado (regla de negocio) → repositorio → `*ResourceFromEntityAssembler` → DTO.

### Contextos

| Contexto | Responsabilidad | Estado |
|---|---|---|
| `shared` | `AuditableAbstractAggregateRoot`, `AuditableModel`, auditoría JPA, OpenAPI, seguridad transitoria, excepciones de dominio base y manejador global de errores (`ErrorResource`). | Implementado |
| `recruitment` | Vacantes (`JobPosting`) y sus criterios ponderados (`EvaluationCriterion`, `Weight`). Ciclo DRAFT → PUBLISHED → CLOSED. | Implementado |
| `iam` | Usuarios, roles (empresa, postulante, admin), emisión y validación de JWT. | Planificado |
| `profiles` | Datos de empresas y postulantes (incluye PII del postulante). | Planificado |
| `interviews` | Entrevista asincrónica: preguntas por vacante, sesiones y respuestas del postulante. | Planificado |
| `evaluation` | Puntuación NLP por criterio con evidencia textual (fragmento + posición), anonimización previa y ranking. ACL hacia el proveedor de IA. | Planificado |

Los nombres de los contextos planificados son una propuesta; ajustar esta tabla cuando se creen.

### Modelo actual de `recruitment`

- `JobPosting` (agregado raíz, tabla `job_postings`): `title`, `description`, `companyId` (VO `CompanyId`),
  `status` (`DRAFT|PUBLISHED|CLOSED`), `closingDate`, `anonymizedScreening`, `criteria`.
- `EvaluationCriterion` (entidad del agregado, tabla `evaluation_criteria`): `name`, `description`,
  `weight` (VO `Weight`, entero 1..100), `criterionType` (`COMPETENCY|CERTIFICATION`) y, solo si es
  `CERTIFICATION`, `certificationName` (obligatorio) y `mandatory`. En `COMPETENCY` esos dos campos se descartan.
- Invariantes del agregado:
  - `publish()` falla si no hay criterios o si la suma de pesos ≠ 100; solo se publica desde DRAFT.
  - Los criterios solo se agregan/editan/eliminan en DRAFT (todos los postulantes se evalúan con los mismos pesos).
  - Nombres de criterio únicos dentro de la vacante (sin distinguir mayúsculas).
  - CLOSED es de solo lectura; no se vuelve a DRAFT; `anonymizedScreening` solo cambia en DRAFT.
  - Una vacante PUBLISHED no se elimina: primero se cierra.

## Reglas que no se rompen

1. **Las reglas de negocio viven en el agregado**, nunca en el controlador ni en el command service.
2. **Los controladores solo reciben y devuelven DTOs** (`resources`), nunca entidades.
3. **Un agregado referencia a otro solo por id** (p. ej. `CompanyId`), nunca con una relación JPA entre agregados.
4. **Commands, queries y value objects son `record`** (los enums de estado/tipo también van en `valueobjects`).
5. **Toda ruta empieza con `/api/v1/` y usa sustantivos en plural en inglés** (`/api/v1/job-postings`).
6. **Ningún contexto importa clases de otro contexto, salvo `shared`.** La integración entre contextos se hace
   con contratos definidos en `shared` o con eventos de integración.
7. Los errores salen siempre con el cuerpo `ErrorResource` del `GlobalExceptionHandler`:
   `ResourceNotFoundException` → 404, `BusinessRuleViolationException` → 422, validación → 400.
8. `open-in-view` está desactivado: los repositorios cargan el agregado completo (`@EntityGraph`).
   En command services no se llama a `save()` sobre agregados ya cargados; se usa `flush()`.
9. Ningún secreto en el repositorio: credenciales solo por variables de entorno o `.env` (ignorado por git).
10. Todo cambio de dominio viene con su prueba unitaria del agregado.

## Servicios externos y sus límites

| Servicio | Uso | Límites y reglas |
|---|---|---|
| **Proveedor de IA / NLP** (por definir) | Puntuar respuestas contra criterios y extraer el fragmento que sustenta cada puntaje. | Solo se invoca desde `evaluation/application/internal/outboundservices` vía `WebClient`. Timeout explícito, reintentos acotados con backoff y manejo de límites de tasa (HTTP 429). Tamaño de prompt y de respuesta acotados. La respuesta se valida: todo puntaje debe traer un fragmento que exista literalmente en la respuesta del postulante; si no, se descarta. Nunca se envía PII (ver abajo). Clave en variable de entorno. |
| **MySQL 8** | Persistencia (`simutalk_db`). | Credenciales por `DB_USERNAME` / `DB_PASSWORD`. `ddl-auto: update` solo para desarrollo. |

Completar esta tabla con el proveedor concreto, su modelo, cuotas y costos cuando se elija.

## Anonimización de datos del postulante (obligatorio)

Antes de enviar **cualquier** texto al proveedor de IA:

- Se eliminan o reemplazan por marcadores (`[NOMBRE]`, `[EMAIL]`, `[TELEFONO]`, `[DOCUMENTO]`, `[DIRECCION]`,
  `[URL]`, etc.) todos los datos personales del postulante: nombre, correo, teléfono, DNI/pasaporte, dirección,
  fecha de nacimiento, fotos, enlaces a perfiles y cualquier dato que lo identifique.
- Al proveedor solo viajan: el texto de la respuesta ya anonimizado, la pregunta y los criterios de la vacante.
  Nunca ids internos del postulante, ni su nombre, ni metadatos de la sesión.
- La anonimización ocurre en el contexto `evaluation`, dentro del ACL de salida, y tiene pruebas unitarias propias.
- Los offsets de evidencia se calculan sobre el texto original para poder mostrar el fragmento real, pero el
  proveedor solo ve el texto anonimizado.
- Si `anonymizedScreening` está activo en la vacante, además se ocultan los datos personales al evaluador humano
  hasta que la empresa decida avanzar con el postulante.

## Ramas y commits

Ramas:

- `main`: versión estable/entregable. Solo recibe merges desde `develop` (o `hotfix/*`).
- `develop`: integración. Base de todo trabajo nuevo.
- `feature/<contexto>-<descripcion-corta>` (p. ej. `feature/interviews-async-session`), desde `develop`.
- `fix/<descripcion>` para correcciones, `hotfix/<descripcion>` desde `main`, `release/<version>` para entregas.
- Todo entra por pull request con revisión; no se hace push directo a `main`.

Commits: [Conventional Commits](https://www.conventionalcommits.org/), en inglés, en imperativo, con el contexto
como scope:

```
feat(recruitment): add weighted evaluation criteria to job postings
fix(shared): return 404 body for unknown routes
docs: describe anonymization requirement
test(recruitment): cover publish weight rule
chore: add maven wrapper
```

Tipos: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `build`, `ci`, `perf`, `style`.
