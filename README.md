# SimuTalk Backend

API REST de **SimuTalk**, plataforma de preselección de talento: cada empresa define criterios de evaluación
ponderados para su vacante, el sistema entrevista de forma asincrónica al postulante, puntúa cada respuesta con NLP
y devuelve un ranking donde cada puntaje se rastrea hasta el fragmento textual que lo sustenta.

Curso 1ASI0705 Arquitectura de Aplicaciones Web — UPC — ciclo 202620.

## Stack

Java 21 · Spring Boot 3.5 · Spring Data JPA + Hibernate · PostgreSQL 16 · Spring Security + JWT · springdoc OpenAPI 3 · Maven

La arquitectura (DDD por bounded contexts), las reglas del proyecto y las convenciones están en [CLAUDE.md](CLAUDE.md).

## Requisitos

- JDK 21
- PostgreSQL 16 en ejecución, con la base `simutalk_db` creada
- No hace falta instalar Maven: se usa el wrapper `./mvnw` (`mvnw.cmd` en Windows)

## Configuración

1. Crea el usuario y la base en PostgreSQL (como superusuario, p. ej. `psql -U postgres`):

   ```sql
   CREATE USER simutalk WITH PASSWORD 'change-me';
   CREATE DATABASE simutalk_db OWNER simutalk;
   ```

2. Copia `.env.example` a `.env` y completa `DB_USERNAME`, `DB_PASSWORD` y `JWT_SECRET`
   (genéralo con `openssl rand -base64 32`). Spring Boot lee `.env` desde la raíz del
   proyecto. También puedes exportar las variables de entorno directamente (o configurarlas en IntelliJ en
   *Run Configuration → Environment variables*).

| Variable | Obligatoria | Por defecto |
|---|---|---|
| `DB_USERNAME` | sí | — |
| `DB_PASSWORD` | sí | — |
| `JWT_SECRET` | sí | — (Base64, ≥ 256 bits) |
| `JWT_EXPIRATION_DAYS` | no | `7` |
| `ADMIN_USERNAME` / `ADMIN_PASSWORD` | no | — (si ambos existen, crea el admin inicial) |
| `CREDENTIALS_MODE` | no | `mock` (`live` llama al emisor; aún sin emisores conectados) |
| `SEED_DEMO_DATA` | no | `false` |
| `DEMO_USERS_PASSWORD` | no | — (contraseña de los usuarios demo; sin ella no pueden iniciar sesión) |
| `DB_HOST` | no | `localhost` |
| `DB_PORT` | no | `5432` |
| `SERVER_PORT` | no | `8080` |

## Ejecución

```bash
./mvnw spring-boot:run        # Linux / macOS
mvnw.cmd spring-boot:run      # Windows
```

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

Pruebas:

```bash
./mvnw test
```

## Autenticación

1. `POST /api/v1/authentication/sign-up` con `{"username", "password", "roles": ["ROLE_RECRUITER"]}`
   (roles permitidos: `ROLE_CANDIDATE`, `ROLE_RECRUITER`; sin roles = `ROLE_CANDIDATE`).
2. `POST /api/v1/authentication/sign-in` → devuelve `token`.
3. En Swagger UI pulsa **Authorize** (candado), pega el token (sin el prefijo `Bearer`) y prueba el resto.

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/v1/authentication/sign-up` | Público |
| POST | `/api/v1/authentication/sign-in` | Público |
| GET | `/api/v1/users` | `ROLE_ADMIN` |
| GET | `/api/v1/users/{userId}` | `ROLE_ADMIN` o el propio usuario |
| GET | `/api/v1/roles` | Autenticado |

## Perfiles y certificaciones (contexto `profiles`, requieren token)

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/v1/company-profiles` | Recruiter (el suyo) o admin |
| GET | `/api/v1/company-profiles/me` | El usuario autenticado (404 si no tiene perfil de empresa) |
| GET | `/api/v1/company-profiles?page=&size=` | Admin (paginado, `page` desde 0, `size` 1 a 100, 20 por defecto) |
| GET | `/api/v1/company-profiles/{id}` | Autenticado |
| PUT | `/api/v1/company-profiles/{id}` | Dueño o admin |
| POST | `/api/v1/candidate-profiles` | Candidato (el suyo) o admin |
| GET | `/api/v1/candidate-profiles/me` | El usuario autenticado (404 si no tiene perfil de postulante) |
| GET | `/api/v1/candidate-profiles?page=&size=` | Admin (paginado, `page` desde 0, `size` 1 a 100, 20 por defecto) |
| GET | `/api/v1/candidate-profiles/{id}` | Dueño, recruiter o admin |
| PUT | `/api/v1/candidate-profiles/{id}` | Dueño o admin |
| POST | `/api/v1/candidate-profiles/{id}/certifications` | Dueño o admin (nace UNVERIFIED) |
| GET | `/api/v1/candidate-profiles/{id}/certifications` | Dueño, recruiter o admin |
| POST | `/api/v1/candidate-profiles/{id}/certifications/{certificationId}/verification` | Dueño o admin |
| DELETE | `/api/v1/candidate-profiles/{id}/certifications/{certificationId}` | Dueño o admin |

Los listados paginados responden `{"content": [...], "page", "size", "totalElements", "totalPages"}`.

Datos demo: arranca con `SEED_DEMO_DATA=true` y `DEMO_USERS_PASSWORD=...`. Usuarios: `consultora.andina` (recruiter),
`rosa.quispe`, `jorge.huaman`, `lucia.flores`, `miguel.condori`, `carmen.ramos`, `diego.salazar` (candidatos).

## Postulaciones (contexto `recruitment`, requieren token)

| Método | Ruta | Acceso |
|---|---|---|
| POST | `/api/v1/job-postings/{jobPostingId}/applications` | Candidato (su propio perfil); solo vacantes PUBLISHED, una vez |
| GET | `/api/v1/job-postings/{jobPostingId}/applications?status=` | Recruiter dueño o admin (pipeline del dashboard) |
| GET | `/api/v1/applications` | Candidato: sus postulaciones |
| PATCH | `/api/v1/applications/{applicationId}/status` | Recruiter dueño o admin; transiciones dirigidas |

Etapas: `RECEIVED → INTERVIEWING → ASSESSED → SHORTLISTED → HIRED`, y `REJECTED` desde cualquier etapa no final.
Un salto inválido responde 422.

## Endpoints de vacantes (contexto `recruitment`, requieren token)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/job-postings` | Crea una vacante en DRAFT a nombre de la empresa del recruiter autenticado |
| GET | `/api/v1/job-postings/{id}` | Obtiene una vacante con sus criterios |
| GET | `/api/v1/job-postings?companyId=&status=` | Lista vacantes (filtros opcionales) |
| PUT | `/api/v1/job-postings/{id}` | Actualiza título, descripción, fecha de cierre y anonimización |
| DELETE | `/api/v1/job-postings/{id}` | Elimina una vacante (no si está PUBLISHED) |
| POST | `/api/v1/job-postings/{id}/criteria` | Agrega un criterio (solo en DRAFT) |
| PUT | `/api/v1/job-postings/{id}/criteria/{criterionId}` | Modifica un criterio (solo en DRAFT) |
| DELETE | `/api/v1/job-postings/{id}/criteria/{criterionId}` | Elimina un criterio (solo en DRAFT) |
| PATCH | `/api/v1/job-postings/{id}/status` | Cambia el estado: `PUBLISHED` (exige pesos = 100) o `CLOSED` |

Escrituras (POST/PUT/DELETE de vacantes y criterios, PATCH de estado): solo el recruiter de la empresa dueña o un
admin; los demás reciben 403. Lecturas: cualquier autenticado, pero un DRAFT solo lo ve su empresa y los listados
muestran de otras empresas solo las PUBLISHED.

Errores: todas las respuestas de error tienen la forma

```json
{
  "timestamp": "2026-09-23T03:52:24.889Z",
  "status": 422,
  "error": "Unprocessable Entity",
  "message": "The weights of the evaluation criteria must add up to exactly 100 (current total: 90)",
  "path": "/api/v1/job-postings/1/status"
}
```

con `details` por campo en los errores de validación (400). 401 = sin token o credenciales inválidas,
403 = sin permiso, 404 = recurso inexistente, 409 = conflicto de unicidad, 422 = regla de negocio.

## Ramas

`main` (estable) · `develop` (integración) · `feature/<contexto>-<descripcion>`. Commits con
[Conventional Commits](https://www.conventionalcommits.org/). Detalle en [CLAUDE.md](CLAUDE.md).
