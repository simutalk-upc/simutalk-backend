# SimuTalk Backend

API REST de **SimuTalk**, plataforma de preselección de talento: cada empresa define criterios de evaluación
ponderados para su vacante, el sistema entrevista de forma asincrónica al postulante, puntúa cada respuesta con NLP
y devuelve un ranking donde cada puntaje se rastrea hasta el fragmento textual que lo sustenta.

Curso 1ASI0705 Arquitectura de Aplicaciones Web — UPC — ciclo 202620.

## Stack

Java 21 · Spring Boot 3.5 · Spring Data JPA + Hibernate · MySQL 8 · Spring Security + JWT · springdoc OpenAPI 3 · Maven

La arquitectura (DDD por bounded contexts), las reglas del proyecto y las convenciones están en [CLAUDE.md](CLAUDE.md).

## Requisitos

- JDK 21
- MySQL 8 en ejecución (la base `simutalk_db` se crea sola al arrancar)
- No hace falta instalar Maven: se usa el wrapper `./mvnw` (`mvnw.cmd` en Windows)

## Configuración

1. Crea un usuario de MySQL (o usa uno existente):

   ```sql
   CREATE USER 'simutalk'@'localhost' IDENTIFIED BY 'change-me';
   GRANT ALL PRIVILEGES ON simutalk_db.* TO 'simutalk'@'localhost';
   ```

2. Copia `.env.example` a `.env` y completa `DB_USERNAME` y `DB_PASSWORD`. Spring Boot lee `.env` desde la raíz del
   proyecto. También puedes exportar las variables de entorno directamente (o configurarlas en IntelliJ en
   *Run Configuration → Environment variables*).

| Variable | Obligatoria | Por defecto |
|---|---|---|
| `DB_USERNAME` | sí | — |
| `DB_PASSWORD` | sí | — |
| `DB_HOST` | no | `localhost` |
| `DB_PORT` | no | `3306` |
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

## Endpoints disponibles (contexto `recruitment`)

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/v1/job-postings` | Crea una vacante en DRAFT |
| GET | `/api/v1/job-postings/{id}` | Obtiene una vacante con sus criterios |
| GET | `/api/v1/job-postings?companyId=&status=` | Lista vacantes (filtros opcionales) |
| PUT | `/api/v1/job-postings/{id}` | Actualiza título, descripción, fecha de cierre y anonimización |
| DELETE | `/api/v1/job-postings/{id}` | Elimina una vacante (no si está PUBLISHED) |
| POST | `/api/v1/job-postings/{id}/criteria` | Agrega un criterio (solo en DRAFT) |
| PUT | `/api/v1/job-postings/{id}/criteria/{criterionId}` | Modifica un criterio (solo en DRAFT) |
| DELETE | `/api/v1/job-postings/{id}/criteria/{criterionId}` | Elimina un criterio (solo en DRAFT) |
| PATCH | `/api/v1/job-postings/{id}/status` | Cambia el estado: `PUBLISHED` (exige pesos = 100) o `CLOSED` |

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

con `details` por campo en los errores de validación (400). 404 = recurso inexistente, 422 = regla de negocio.

> **Seguridad:** el contexto `iam` (JWT) aún no está implementado; mientras tanto las rutas `/api/v1/**` están
> abiertas para poder probarlas desde Swagger UI.

## Ramas

`main` (estable) · `develop` (integración) · `feature/<contexto>-<descripcion>`. Commits con
[Conventional Commits](https://www.conventionalcommits.org/). Detalle en [CLAUDE.md](CLAUDE.md).
