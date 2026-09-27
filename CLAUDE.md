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
| Persistencia | Spring Data JPA + Hibernate, PostgreSQL 16 (`simutalk_db`) |
| Seguridad | Spring Security + JWT HS256 (jjwt 0.12.6), BCrypt — contexto `iam` |
| Documentación | OpenAPI 3 con springdoc 2.8.5 (Swagger UI en `/swagger-ui.html`) |
| Utilidades | Lombok, ModelMapper 3.2.1 |
| Build | Maven (usar siempre `./mvnw`) |

Comandos:

```bash
./mvnw test                 # pruebas unitarias (no requieren base de datos)
./mvnw spring-boot:run      # levanta la API (requiere PostgreSQL, DB_USERNAME, DB_PASSWORD y JWT_SECRET)
```

## Arquitectura: DDD con bounded contexts

Paquete raíz `pe.upc.simutalk`. Cada bounded context es un paquete de primer nivel con **cuatro capas**:

| Capa | Paquete | Contenido |
|---|---|---|
| **Domain** | `domain/model/{aggregates,entities,commands,queries,valueobjects}`, `domain/services` | Agregados, entidades, value objects, commands, queries e interfaces de servicios. Aquí viven las reglas de negocio. Sin dependencias de web. |
| **Application** | `application/internal/{commandservices,queryservices,outboundservices,eventhandlers}` | Implementaciones de los servicios de dominio: cargan el agregado, delegan en él y persisten. `outboundservices` son puertos/ACL hacia sistemas externos (interfaces implementadas en infrastructure). `eventhandlers` reaccionan a eventos de la aplicación. |
| **Infrastructure** | `infrastructure/persistence/jpa/repositories` (+ adaptadores técnicos, p. ej. `iam/infrastructure/{hashing,tokens,authorization}`) | Repositorios Spring Data JPA e implementaciones de los puertos de salida. |
| **Interfaces** | `interfaces/rest/{resources,transform}`, `interfaces/acl` | Controladores REST, DTOs (`resources`) y ensambladores DTO ↔ command/entidad (`transform`). `acl` implementa los contratos que el contexto publica en `shared/interfaces/acl`. |

Flujo de una escritura: `Controller` → `*CommandFromResourceAssembler` → `Command` → `CommandService` →
agregado (regla de negocio) → repositorio → `*ResourceFromEntityAssembler` → DTO.

### Contextos

| Contexto | Responsabilidad | Estado |
|---|---|---|
| `shared` | `AuditableAbstractAggregateRoot`, `AuditableModel`, estrategia de nombres snake_case con tablas en plural, OpenAPI, excepciones de dominio base, manejador global de errores (`ErrorResource`), `MessageResource` y contratos ACL entre contextos (`IamContextFacade`). | Implementado |
| `recruitment` | Vacantes (`JobPosting`) y sus criterios ponderados (`EvaluationCriterion`, `Weight`). Ciclo DRAFT → PUBLISHED → CLOSED. | Implementado |
| `iam` | Usuarios (`User`), roles (`Role`, `Roles`), registro, sign-in con JWT, autorización y `IamContextFacadeImpl`. | Implementado |
| `profiles` | Datos de empresas y postulantes (incluye PII del postulante). | Planificado |
| `interviews` | Entrevista asincrónica: preguntas por vacante, sesiones y respuestas del postulante. | Planificado |
| `assessment` | Puntuación NLP por criterio con evidencia textual (fragmento + posición), anonimización previa y ranking. ACL hacia el proveedor de IA. | Planificado |

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

### Modelo actual de `iam`

- `User` (agregado raíz, tabla `users`): `username` único, `password` (siempre hash BCrypt), `roles`
  (`@ManyToMany` EAGER vía `user_roles`). Sin setters; roles inmutables hacia afuera.
- `Role` (entidad, tabla `roles`): `name` de tipo `Roles` = `ROLE_ADMIN` (soporte), `ROLE_RECRUITER` (usuario de la
  empresa que publica vacantes y define criterios), `ROLE_CANDIDATE` (postulante). Se siembran al arrancar.
- Invariantes: `addRoles` rechaza lista vacía; el registro público (`addSignUpRoles`) nunca concede `ROLE_ADMIN`;
  sin roles se asigna `ROLE_CANDIDATE`.
- Sign-in fallido responde siempre 401 "Invalid username or password", exista o no el usuario.

## Seguridad

- Rutas públicas: `/api/v1/authentication/**` y la documentación (`/v3/api-docs/**`, `/swagger-ui/**`). Todo lo
  demás exige `Authorization: Bearer <jwt>`.
- JWT HS256, `subject` = username. El secreto es Base64 de ≥ 256 bits en `JWT_SECRET` (sin valor por defecto: sin
  él la app no arranca). Expiración en `JWT_EXPIRATION_DAYS` (7 por defecto).
- Autorización fina con `@PreAuthorize` (`@EnableMethodSecurity`). `UserDetailsImpl` expone `id` para reglas del
  tipo `#userId == authentication.principal.id`.
- El único camino para crear un administrador es el bootstrap al arrancar con `ADMIN_USERNAME` y `ADMIN_PASSWORD`
  (idempotente: si existe, no lo toca).
- CORS abierto en desarrollo; en producción se restringe al dominio del frontend Angular (TODO en
  `WebSecurityConfiguration`).
- Para saber quién es un usuario desde otro contexto se usa `shared.interfaces.acl.IamContextFacade`, nunca
  `UserRepository` ni clases de `iam`.

## Reglas que no se rompen

1. **Las reglas de negocio viven en el agregado**, nunca en el controlador ni en el command service.
2. **Los controladores solo reciben y devuelven DTOs** (`resources`), nunca entidades.
3. **Un agregado referencia a otro solo por id** (p. ej. `CompanyId`), nunca con una relación JPA entre agregados.
4. **Commands, queries y value objects son `record`** (los enums de estado/tipo también van en `valueobjects`).
5. **Toda ruta empieza con `/api/v1/` y usa sustantivos en plural en inglés** (`/api/v1/job-postings`).
6. **Ningún contexto importa clases de otro contexto, salvo `shared`.** La integración entre contextos se hace
   con contratos (fachadas ACL) definidos en `shared/interfaces/acl` e implementados en
   `<contexto>/interfaces/acl`, o con eventos de integración.
7. Los errores salen siempre con el cuerpo `ErrorResource` del `GlobalExceptionHandler`:
   validación / argumento inválido → 400, `InvalidCredentialsException` o sin token → 401, sin permiso → 403,
   `ResourceNotFoundException` → 404, conflicto de unicidad → 409, `BusinessRuleViolationException` → 422.
8. `open-in-view` está desactivado: los repositorios cargan el agregado completo (`@EntityGraph`).
   En command services no se llama a `save()` sobre agregados ya cargados; se usa `flush()`.
9. Ningún secreto en el repositorio: credenciales, `JWT_SECRET` y llaves de API solo por variables de entorno o
   `.env` (ignorado por git). Nada de valores reales en `application.yml`.
10. Todo cambio de dominio viene con su prueba unitaria del agregado.
11. Entidades JPA: constructor protegido sin argumentos, `@Getter` donde haga falta, nunca `@Data` ni setters
    públicos. Siempre `jakarta.persistence`, nunca `javax.persistence`.

## Servicios externos y sus límites

| Servicio | Uso | Límites y reglas |
|---|---|---|
| **Proveedor de IA / NLP** (por definir) | Puntuar respuestas contra criterios y extraer el fragmento que sustenta cada puntaje. | Solo se invoca desde `assessment/application/internal/outboundservices` vía `WebClient`. Timeout explícito, reintentos acotados con backoff y manejo de límites de tasa (HTTP 429). Tamaño de prompt y de respuesta acotados. La respuesta se valida: todo puntaje debe traer un fragmento que exista literalmente en la respuesta del postulante; si no, se descarta. Nunca se envía PII (ver abajo). Clave en variable de entorno. |
| **PostgreSQL 16** | Persistencia (`simutalk_db`). | Credenciales por `DB_USERNAME` / `DB_PASSWORD`. `ddl-auto: update` solo para desarrollo. |

Completar esta tabla con el proveedor concreto, su modelo, cuotas y costos cuando se elija.

## Anonimización de datos del postulante (obligatorio)

Antes de enviar **cualquier** texto al proveedor de IA:

- Se eliminan o reemplazan por marcadores (`[NOMBRE]`, `[EMAIL]`, `[TELEFONO]`, `[DOCUMENTO]`, `[DIRECCION]`,
  `[URL]`, etc.) todos los datos personales del postulante: nombre, correo, teléfono, DNI/pasaporte, dirección,
  fecha de nacimiento, fotos, enlaces a perfiles y cualquier dato que lo identifique.
- Al proveedor solo viajan: el texto de la respuesta ya anonimizado, la pregunta y los criterios de la vacante.
  Nunca ids internos del postulante, ni su nombre, ni metadatos de la sesión.
- La anonimización ocurre en el contexto `assessment`, dentro del ACL de salida, y tiene pruebas unitarias propias.
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
