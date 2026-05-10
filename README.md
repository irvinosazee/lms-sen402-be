# LMS Backend — Spring Boot API

This is the core of the Library Management System. It exposes a REST API at `http://localhost:8081/api/v1/...` that the frontend (and any other client — curl, Postman, etc.) talks to.

## Quick links

- **Setup:** [`docs/01-GETTING-STARTED.md`](./docs/01-GETTING-STARTED.md) — local setup in 5 steps.
- **Beginner guide:** [`docs/02-SPRING_BOOT_CRASH_COURSE.md`](./docs/02-SPRING_BOOT_CRASH_COURSE.md) — every Spring Boot concept this project uses, explained from zero.
- **What's built:** [`docs/03-CURRENT_IMPLEMENTATION.md`](./docs/03-CURRENT_IMPLEMENTATION.md).
- **Fine system:** [`docs/04-OVERDUE_FINES.md`](./docs/04-OVERDUE_FINES.md).
- **API contract:** [`../../docs/API_REFERENCE.md`](../../docs/API_REFERENCE.md) — every endpoint with curl examples.
- **Schema:** [`../../docs/DATABASE_SCHEMA.md`](../../docs/DATABASE_SCHEMA.md) — every table.
- **Security model:** [`../../docs/SECURITY_ARCHITECTURE.md`](../../docs/SECURITY_ARCHITECTURE.md) — JWT, BCrypt, env vars, what NOT to commit.

## Tech stack

| Layer | Choice |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.3.5 |
| Security | Spring Security 6 + JWT (HS256) |
| Persistence | Spring Data JPA + Hibernate 6 |
| Database | PostgreSQL 14+ |
| Build | Maven (via `./mvnw`) |
| Tests | JUnit 5 + Mockito |

## Run it

```bash
# One-time
createdb lms-project
cp .env.example .env
# Edit .env: set DB_PASSWORD and a fresh JWT_SECRET (openssl rand -base64 32)

# Every time
set -a; source .env; set +a
./mvnw spring-boot:run         # API on http://localhost:8081
```

See [`docs/01-GETTING-STARTED.md`](./docs/01-GETTING-STARTED.md) for details and [`../../docs/DEVELOPMENT_GUIDE.md#troubleshooting`](../../docs/DEVELOPMENT_GUIDE.md#troubleshooting) for when things break.

## Run tests

```bash
./mvnw test                    # → 17 tests, all green
```

## Project layout (heavily abridged)

```
apps/backend/
├── .env                ← your secrets (gitignored)
├── .env.example        ← committed template
├── pom.xml             ← Maven project file: dependencies, build config
├── mvnw, mvnw.cmd      ← Maven Wrapper scripts
├── src/
│   ├── main/
│   │   ├── java/com/lms/
│   │   │   ├── LmsBackendApplication.java    ← entry point (the @SpringBootApplication)
│   │   │   ├── controllers/                  ← @RestController classes (HTTP layer)
│   │   │   ├── services/                     ← @Service classes (business logic)
│   │   │   ├── repositories/                 ← Spring Data JPA interfaces
│   │   │   ├── entities/                     ← @Entity classes (DB tables)
│   │   │   ├── dtos/                         ← request/response shapes
│   │   │   ├── exceptions/                   ← custom exceptions + @ControllerAdvice
│   │   │   ├── security/                     ← JwtService, JwtAuthenticationFilter, SecurityConfig
│   │   │   ├── config/                       ← WebConfig (CORS), DataInitializer
│   │   │   └── specifications/               ← BookSpecification (JPA Specification API for search)
│   │   └── resources/
│   │       └── application.yml               ← config: reads from env vars
│   └── test/java/com/lms/
│       ├── services/                         ← unit tests with Mockito
│       └── LmsBackendApplicationTests.java   ← context-loads sanity test
└── docs/                                     ← backend-specific docs
```

If you're reading the codebase for the first time, start in this order:

1. `application.yml` — see what config exists.
2. `LmsBackendApplication.java` — the entry point.
3. `security/SecurityConfig.java` — see how auth is wired.
4. `controllers/LoanController.java` — pick a controller and follow it down.
5. `services/LoanService.java` — see the business logic.
6. `entities/Loan.java` + `repositories/LoanRepository.java` — see the data layer.

## What's secured

All endpoints **except** `/api/v1/auth/**` (login, register) require a valid JWT in the `Authorization` header:

```
Authorization: Bearer eyJhbGci...
```

The token is obtained from `POST /api/v1/auth/login` (see [API reference](../../docs/API_REFERENCE.md#authentication)).

Each endpoint may additionally require a specific role — see the `@PreAuthorize` annotations on controller methods.

---

For setup help, troubleshooting, and onboarding: **[../../docs/DEVELOPMENT_GUIDE.md](../../docs/DEVELOPMENT_GUIDE.md)**.
