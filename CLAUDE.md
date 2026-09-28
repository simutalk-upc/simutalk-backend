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
| Framework | Spring Boot 4.1.x (webmvc, data-jpa, security, validation); llamadas HTTP salientes con `RestClient` (`spring-boot-starter-restclient`) |
| Persistencia | Spring Data JPA + Hibernate, PostgreSQL 16 (`simutalk_db`) |
| Seguridad | Spring Security + JWT HS256 (jjwt 0.12.6), BCrypt — contexto `iam` |
| Documentación | OpenAPI 3.1 con springdoc 3.1.1 (Swagger UI en `/swagger-ui.html`) |
| JSON | Jackson 3 (`tools.jackson`; las anotaciones siguen en `com.fasterxml.jackson.annotation`), con `spring.jackson.use-jackson2-defaults=true` |
| Utilidades | Lombok |
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
| `shared` | `AuditableAbstractAggregateRoot`, `AuditableModel`, estrategia de nombres snake_case con tablas en plural, OpenAPI, excepciones de dominio base, manejador global de errores (`ErrorResource`), `PageResource`, contratos ACL entre contextos (`IamContextFacade`, `ProfilesContextFacade`, `RecruitmentContextFacade`, `InterviewsContextFacade`, `AssessmentContextFacade`), eventos de integración (`shared/interfaces/events`, p. ej. `InterviewSessionCompletedEvent`) y el cliente del proveedor de IA (`shared/infrastructure/external/ai/GenerativeAiClient`, solo transporte, sin dominio). | Implementado |
| `recruitment` | Vacantes (`JobPosting`) y sus criterios ponderados (`EvaluationCriterion`, `Weight`), ciclo DRAFT → PUBLISHED → CLOSED; postulaciones (`Application`) y su pipeline. | Implementado |
| `iam` | Usuarios (`User`), roles (`Role`, `Roles`), registro, sign-in con JWT, autorización y `IamContextFacadeImpl`. | Implementado |
| `profiles` | Perfiles de empresa (`CompanyProfile`) y de postulante (`CandidateProfile`, incluye PII), certificaciones (`Certification`) y su verificación con el emisor. `ProfilesContextFacadeImpl`. | Implementado |
| `interviews` | Guion de preguntas por vacante (`Question`), sesión de entrevista asincrónica (`InterviewSession`) y respuestas (`Answer`). Solo registra qué se preguntó y qué se respondió; nada de puntuación ni IA. `InterviewsContextFacadeImpl`. | Implementado |
| `analytics` | Reportes (embudo, promedios por criterio, emisiones evitadas, resumen de empresa) y `CarbonSaving`. Solo agregaciones en base de datos. | Implementado |
| `assessment` | Evaluación por criterio (`Assessment`, `CriterionScore`, `Evidence`, `IntegrityFlag`) con evidencia textual anclada, anonimización previa (`TranscriptAnonymizer`), puerto de IA (`AnswerScoringService`, adaptador mock/Gemini) y ranking explicable. | Implementado |

Los nombres de los contextos planificados son una propuesta; ajustar esta tabla cuando se creen.

### Modelo actual de `recruitment`

- `JobPosting` (agregado raíz, tabla `job_postings`): `title`, `description`, `companyId` (VO `CompanyId`),
  `status` (`DRAFT|PUBLISHED|CLOSED`), `closingDate`, `anonymizedScreening`, `criteria`.
- `EvaluationCriterion` (entidad del agregado, tabla `evaluation_criteria`): `name`, `description`,
  `weight` (VO `Weight`, entero 1..100), `criterionType` (`COMPETENCY|CERTIFICATION`) y, solo si es
  `CERTIFICATION`, `certificationName` (obligatorio) y `mandatory`. En `COMPETENCY` esos dos campos se descartan.
  `origin` (`CriterionOrigin`: `AI_SUGGESTED|MANUAL`, MANUAL por defecto; las filas anteriores leen MANUAL).
- Sugerencia de criterios (US-04): `POST /api/v1/job-postings/{id}/criteria/suggestions`, solo el recruiter dueño y
  con la vacante en DRAFT (`ensureCriteriaCanBeSuggested()` en el agregado). Devuelve `CriterionSuggestion` (nombre,
  descripción, justificación) SIN persistir y SIN peso: **el sistema nunca asigna un peso**; el reclutador acepta con
  `POST /criteria`, poniendo él el peso y `origin=AI_SUGGESTED`. Puerto `CriterionSuggestionService`; en mock, 4
  competencias por palabras clave de la descripción; en live, Gemini (se ignora cualquier peso que devuelva).
- Invariantes del agregado:
  - `publish()` falla si no hay criterios, si la suma de pesos ≠ 100 o si algún criterio COMPETENCY no tiene
    ninguna pregunta de entrevista (el agregado recibe un `InterviewQuestionCounter`, alimentado por
    `InterviewsContextFacade`); solo se publica desde DRAFT.
  - Los criterios solo se agregan/editan/eliminan en DRAFT (todos los postulantes se evalúan con los mismos pesos).
  - Nombres de criterio únicos dentro de la vacante (sin distinguir mayúsculas).
  - CLOSED es de solo lectura; no se vuelve a DRAFT; `anonymizedScreening` solo cambia en DRAFT.
  - Una vacante PUBLISHED no se elimina: primero se cierra.
  - Visibilidad: un DRAFT solo lo ve su propia empresa; en los listados, las vacantes de otras empresas solo
    aparecen mientras están PUBLISHED (`isVisibleTo` / `isListedFor` con el VO `JobPostingViewer`).
- `Application` (agregado raíz, tabla `applications`): `jobPostingId` y `candidateId` (solo ids), `status`
  (`ApplicationStatus`), `appliedAt`, `shortlistedAt` (momento en que llegó a SHORTLISTED, para el tiempo a la terna). Solo se postula a una vacante PUBLISHED y una vez por candidato (también
  `UNIQUE (job_posting_id, candidate_id)`). Transiciones dirigidas: RECEIVED → INTERVIEWING → ASSESSED →
  SHORTLISTED → HIRED, y REJECTED desde cualquier etapa no final; REJECTED y HIRED son finales. Un salto inválido
  lanza `InvalidStateTransitionException` (una `IllegalStateException`, 422 en la API).
- Con `app.seed-demo-data=true`, los datos demo se siembran en una sola secuencia de listeners de
  `ApplicationReadyEvent` ordenados con `@Order`, cada paso en su contexto y en su propia transacción: 100 `profiles`
  (empresa y 6 candidatos) → 200 `recruitment` (vacante en DRAFT con criterios) → 300 `interviews` (guion) → 400
  `recruitment` (publica y hace postular a los 6) → 500 `interviews` (sesiones: 2 COMPLETED, 2 IN_PROGRESS) → 600
  `assessment` (evalúa las completadas, solo con el motor mock). Cada paso encuentra lo que dejó el anterior por las
  fachadas de `shared` y revisa su propia precondición, así que es idempotente (una sesión solo se crea sobre una
  postulación en RECEIVED). Un fallo de cualquier paso se registra como WARN y nunca impide que la app arranque.
  No hay eventos de demo en `shared`.
- `RecruitmentContextFacadeImpl` expone vacantes, criterios y postulaciones a otros contextos, y mueve postulaciones
  a INTERVIEWING / ASSESSED siempre a través del agregado `Application`.

### Modelo actual de `iam`

- `User` (agregado raíz, tabla `users`): `username` único, `password` (siempre hash BCrypt), `roles`
  (`@ManyToMany` EAGER vía `user_roles`). Sin setters; roles inmutables hacia afuera.
- `Role` (entidad, tabla `roles`): `name` de tipo `Roles` = `ROLE_ADMIN` (soporte), `ROLE_RECRUITER` (usuario de la
  empresa que publica vacantes y define criterios), `ROLE_CANDIDATE` (postulante). Se siembran al arrancar.
- Invariantes: `addRoles` rechaza lista vacía; el registro público (`addSignUpRoles`) nunca concede `ROLE_ADMIN`;
  sin roles se asigna `ROLE_CANDIDATE`.
- Sign-in fallido responde siempre 401 "Invalid username or password", exista o no el usuario.

### Modelo actual de `profiles`

- `CompanyProfile` (tabla `companies`): `userId` (solo el id, sin relación con `iam`), `legalName`, `tradeName`,
  `industry`, `ruc` (VO `Ruc`: 11 dígitos, empieza en 10 o 20; no cambia), `companySize`
  (`MICRO|PEQUENA|MEDIANA|GRANDE`), `district`.
- `CandidateProfile` (tabla `candidates`): `userId`, `personName` (VO `PersonName`), `documentNumber`
  (VO `DocumentNumber`: DNI 8 dígitos o CE 9 a 12; no cambia), `birthDate` (mínimo 18 años), `phone`, `district`,
  `yearsOfExperience` (≥ 0) y `certifications`.
- `Certification` (tabla `certifications`, entidad del agregado candidato): `title`, `issuer`, `credentialCode`
  (opcional), `issuedAt`, `expiresAt` (opcional), `verificationStatus` (`VERIFIED|UNVERIFIED|REJECTED`), `verifiedAt`.
  Nace UNVERIFIED y **nunca queda VERIFIED sin código de credencial**. `countsForScoring()` = VERIFIED y no expirada.
- Un usuario tiene como máximo un perfil (de empresa o de candidato) y debe existir en `iam` (vía `IamContextFacade`).
- Verificación: coincide → VERIFIED; no coincide → REJECTED; emisor no disponible → sigue UNVERIFIED. Solo desde
  UNVERIFIED. Sin código responde 422 y no se consulta al emisor.
- Nada de puntuación aquí: cuánto vale una certificación lo decide `assessment` (vía `ProfilesContextFacade`).
- `app.seed-demo-data=true` siembra datos demo (1 empresa, 6 candidatos) si las tablas de perfiles están vacías.

### Modelo actual de `interviews`

- `Question` (agregado raíz, tabla `questions`): `jobPostingId` y `criterionId` (solo ids), `statement` (1 a 500
  caracteres), `maxDurationSeconds` (30 a 600), `position` (≥ 1, consecutivas dentro del guion), `origin`
  (`AI_SUGGESTED|MANUAL`), `allowsFollowUp`.
- `InterviewSession` (agregado raíz, tabla `interview_sessions`): `applicationId` (único: una sesión por
  postulación), `jobPostingId` y `candidateId` (copias inmutables tomadas de la postulación, para autorización),
  `status` (`PENDING|IN_PROGRESS|COMPLETED|EXPIRED`), `invitedAt`, `startedAt`, `finishedAt`, `expiresAt`,
  `totalDurationSeconds` (suma de la duración de las respuestas) y `answers`.
  - `start()` solo desde PENDING y sin vencer; `recordAnswer()` solo en IN_PROGRESS, una respuesta por pregunta y a
    lo sumo una repregunta por pregunta, solo si la pregunta la permite y con su respuesta padre; `complete()` solo
    desde IN_PROGRESS y con todas las preguntas del guion respondidas; `expire()` desde PENDING o IN_PROGRESS una
    vez vencida. COMPLETED y EXPIRED son finales. Los saltos inválidos lanzan `InvalidStateTransitionException`.
- `Answer` (entidad de la sesión, tabla `answers`): `questionId`, `parentAnswerId` (solo en repreguntas),
  `transcript` (no vacío), `audioUrl`, `durationSeconds`, `answeredAt`, `isFollowUp`.
- Reglas que cruzan contextos (en los servicios de aplicación, vía `RecruitmentContextFacade`): el guion solo cambia
  con la vacante en DRAFT (publicada queda congelado, como los pesos); una pregunta apunta a un criterio COMPETENCY de
  la misma vacante (uno de CERTIFICATION responde 422 con mensaje explícito); crear la sesión exige una postulación en
  RECEIVED y la mueve a INTERVIEWING; completarla la mueve a ASSESSED.
- Aún no hay un proceso que marque como EXPIRED las sesiones vencidas: `expire()` existe en el agregado, pero nada lo
  invoca todavía.

### Modelo actual de `assessment`

- `Assessment` (agregado raíz, tabla `assessments`): `interviewSessionId` (único), copias inmutables de
  `applicationId`, `jobPostingId` y `candidateId`, `weightedScore` (0.0 a 10.0), `engineVersion`, `computedAt`,
  `criterionScores`, `integrityFlags` y `feedbackSummary` (retroalimentación para el candidato, hasta 2000 caracteres:
  qué sostuvo el puntaje y en qué criterio se perdieron más puntos; `null` en evaluaciones anteriores). Solo se calcula sobre una sesión COMPLETED. `weightedScore = Σ(score ×
  weightApplied) / 100`, calculado dentro del agregado y redondeado a 1 decimal (HALF_UP); los pesos suman 100.
- `CriterionScore` (tabla `criterion_scores`): `criterionId`, `criterionName` (copia), `criterionKind`
  (`COMPETENCY|CERTIFICATION`), `score`, `weightApplied`, `confidence`, `evidences`. Un COMPETENCY necesita al menos
  una evidencia; un CERTIFICATION no: su puntaje sale de las certificaciones verificadas y vigentes
  (`CertificationScoringPolicy`: 0 → 0.0, 1 → 7.0, 2 → 8.5, 3+ → 10.0).
- `Evidence` (tabla `evidences`): `answerId`, `excerpt`, `startOffset`, `endOffset`. El excerpt es literalmente
  `transcript[start, end)` del texto ORIGINAL.
- `IntegrityFlag` (tabla `integrity_flags`): `AI_GENERATED_CONTENT` (respuestas que parecen redactadas por IA) y
  `CV_INCONSISTENCY` (certificaciones declaradas que el emisor rechazó); severidad `LOW|MEDIUM|HIGH`. No cambian el
  puntaje.
- Flujo: cada respuesta se anonimiza, se puntúa con `AnswerScoringService` (solo transcript anonimizado y criterio),
  el fragmento devuelto se vuelve a ubicar en el texto anonimizado y se traduce al original. Si el proveedor no da
  evidencia para un criterio COMPETENCY, no se guarda nada (422).
- Retroalimentación (US-22): al calcular, `AnswerScoringService.summarizeFeedback` recibe solo los puntajes, pesos y
  los fragmentos ANONIMIZADOS (nunca el ranking, otros candidatos ni las señales de integridad); en mock, o si el
  proveedor falla, un texto determinista a partir de los números.
- Ranking explicable (`RankingPolicy`): orden por `weightedScore`, desglose por criterio, insignias (`TOP_RANKED`,
  `STRONG_EVIDENCE`, `VERIFIED_CERTIFICATIONS`, `MISSING_MANDATORY_CERTIFICATION`, `INTEGRITY_ALERT`) y códigos
  estables `CANDIDATO-X-9999` (HMAC con `app.anonymization.secret`). Si la vacante tiene `anonymizedScreening`, la
  anonimización es forzada y además se redactan los datos personales dentro de los excerpts.

### Modelo actual de `analytics`

- `CarbonSaving` (agregado raíz, tabla `carbon_savings`): `applicationId` (único), copias de `jobPostingId` y
  `companyId`, `distanceKm` (ida y vuelta), `emissionFactor` (kg CO2e/km), `kgCo2eAvoided = distanceKm ×
  emissionFactor` (calculado en el agregado, 3 decimales) y `computedAt`. Se registra cuando `interviews` publica
  `InterviewSessionCompletedEvent`, después del commit y en una transacción propia (si falla, no deshace la entrevista).
- Distancia (`CommuteDistancePolicy`): distancia en línea recta entre los centroides de los distritos del candidato y
  de la empresa × `road-factor` × 2. Factor, road-factor, distancia por defecto y centroides en `sustainability.*`;
  el factor de emisión y los centroides son valores de referencia que el equipo debe validar antes de reportar.
- Reportes: todo sale de agregaciones en base de datos (JPQL `COUNT`, `AVG`, `SUM` con `GROUP BY`). Las que tocan
  datos de otro contexto se calculan en ese contexto y se exponen por su fachada; `analytics` no lee sus tablas.
  "Tiempo medio hasta la terna" = promedio de días entre la postulación y SHORTLISTED.

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
  `UserRepository` ni clases de `iam`. Así lo hace `ProfileAccessPolicy` (`@profileAccess` en `@PreAuthorize`).
- `recruitment`: toda escritura (vacantes, criterios, estado) exige `ROLE_RECRUITER` dueño de la vacante o
  `ROLE_ADMIN`; pedir sugerencias de criterios es solo del recruiter dueño. El dueño se resuelve en `RecruitmentAccessPolicy` (`@recruitmentAccess`): username →
  `IamContextFacade` → userId → `ProfilesContextFacade.fetchCompanyIdByUserId` → companyId de la vacante. Al crear,
  el `companyId` sale del usuario autenticado, nunca del cuerpo. Las lecturas siguen abiertas a cualquier
  autenticado con las reglas de visibilidad del agregado.
- Postulaciones: solo `ROLE_CANDIDATE` postula (el `candidateId` sale del usuario autenticado vía
  `ProfilesContextFacade.fetchCandidateIdByUserId`, nunca del cuerpo) y lista las suyas en `/api/v1/applications`;
  el pipeline de una vacante y el cambio de etapa son del recruiter dueño de la vacante o de un admin.
- `interviews` (`InterviewsAccessPolicy`, `@interviewsAccess`): el recruiter dueño de la vacante (o un admin) gestiona
  el guion e invita; el guion lo lee el dueño o el candidato con una sesión IN_PROGRESS en esa vacante; solo el
  candidato dueño inicia, responde y completa su entrevista; las respuestas las leen el candidato dueño y el recruiter
  de la vacante.
- `assessment` (`AssessmentAccessPolicy`, `@assessmentAccess`): calcular evaluaciones y leer evidencias y el ranking
  es solo del recruiter dueño de la vacante o de un admin. `GET /interview-sessions/{id}/assessment` también lo lee el
  candidato dueño de la entrevista, pero con otra vista (`CandidateAssessmentResource`): su puntaje ponderado, su
  desglose por criterio y la retroalimentación; nunca su posición en el ranking, puntajes de otros ni señales de
  integridad.
- `analytics` (`AnalyticsAccessPolicy`, `@analyticsAccess`): los reportes de una vacante son del recruiter de la
  empresa dueña o de un admin; el resumen de una empresa, del recruiter de esa empresa o de un admin.
- `profiles`: un candidato solo lee y modifica su propio perfil y certificaciones; un recruiter lee perfiles y
  certificaciones de candidatos pero nunca los edita, y gestiona su propio perfil de empresa; un admin puede todo.
  El perfil propio se lee en `/candidate-profiles/me` y `/company-profiles/me` (el usuario sale del token, nunca de
  un parámetro); las rutas de colección listan de verdad, paginadas, y solo para admin.

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
   `ResourceNotFoundException` → 404, conflicto de unicidad → 409, `BusinessRuleViolationException` e
   `InvalidStateTransitionException` → 422.
8. `open-in-view` está desactivado: los repositorios cargan el agregado completo (`@EntityGraph`).
   En command services no se llama a `save()` sobre agregados ya cargados; se usa `flush()`.
9. Ningún secreto en el repositorio: credenciales, `JWT_SECRET` y llaves de API solo por variables de entorno o
   `.env` (ignorado por git). Nada de valores reales en `application.properties`.
10. Todo cambio de dominio viene con su prueba unitaria del agregado.
11. Entidades JPA: constructor protegido sin argumentos, `@Getter` donde haga falta, nunca `@Data` ni setters
    públicos. Siempre `jakarta.persistence`, nunca `javax.persistence`.

## Servicios externos y sus límites

| Servicio | Uso | Límites y reglas |
|---|---|---|
| **Proveedor de IA / NLP**: Google Gemini (`generateContent`) | Puntuar respuestas contra criterios y extraer el fragmento que sustenta cada puntaje; sugerir criterios a partir de la descripción del puesto. | Un solo cliente de transporte, `shared/infrastructure/external/ai/GenerativeAiClient` (vía `RestClient`): recibe un prompt y devuelve texto; concentra `external.ai.mode` = `mock` (por defecto, sin red) o `live` (requiere `GEMINI_API_KEY`; modelo en `GEMINI_MODEL`), timeout, reintentos acotados con backoff ante 429/503 y caché LRU por SHA-256 del modelo y el prompt. Cada contexto conserva su puerto y su adaptador en `<contexto>/infrastructure/external/ai`, que arma su prompt, valida la respuesta contra su esquema y en mock responde con su propia lógica: `assessment` (`AnswerScoringService`) y `recruitment` (`CriterionSuggestionService`). La anonimización ocurre en el adaptador, antes del cliente, nunca dentro de él. Tamaño de prompt y de respuesta acotados. La respuesta se valida: todo puntaje debe traer un fragmento que exista literalmente en la respuesta del postulante; si no, se descarta. Nunca se envía PII (ver abajo). Clave en variable de entorno. |
| **Verificación de credenciales** (Coursera, Credly, CertiProf) | Confirmar que una certificación declarada existe. | `profiles/infrastructure/external/credentials`. `external.credentials.mode` = `mock` (por defecto, sin red: código ≥ 8 caracteres coincide) o `live` (`RestClient`; emisores aún sin conectar, TODO por emisor, nunca inventar endpoints). Timeout, reintento con backoff exponencial ante 429 y respuesta de reserva que deja la certificación en UNVERIFIED. Al emisor solo viajan emisor, código, título y nombre del titular (necesario para el cotejo); nada de eso va al proveedor de IA. |
| **PostgreSQL 16** | Persistencia (`simutalk_db`). | Credenciales por `DB_USERNAME` / `DB_PASSWORD`. `ddl-auto: update` solo para desarrollo. |

Pendiente: documentar las cuotas y costos del plan de Gemini que use el equipo.

## Anonimización de datos del postulante (obligatorio)

Antes de enviar **cualquier** texto al proveedor de IA:

- Se eliminan o reemplazan por marcadores (`[NOMBRE]`, `[EMAIL]`, `[TELEFONO]`, `[DOCUMENTO]`, `[DIRECCION]`,
  `[URL]`, etc.) todos los datos personales del postulante: nombre, correo, teléfono, DNI/pasaporte, dirección,
  fecha de nacimiento, fotos, enlaces a perfiles y cualquier dato que lo identifique.
- Al proveedor solo viajan: el texto de la respuesta ya anonimizado, la pregunta y los criterios de la vacante.
  Nunca ids internos del postulante, ni su nombre, ni metadatos de la sesión.
- La anonimización ocurre en el contexto `assessment`, dentro del ACL de salida (`TranscriptAnonymizer`), y tiene
  pruebas unitarias propias; `AnswerScoringPrivacyTest` verifica el payload construido y el cuerpo HTTP enviado.
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
