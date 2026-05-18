# Backend — Getting Started

This is a short, backend-only setup guide. For the **full project setup** (Java/Postgres/Bun installation, troubleshooting, etc.), read [`DEVELOPMENT_GUIDE.md`](../../../docs/DEVELOPMENT_GUIDE.md) first.

---

## What you need

| Tool | Version | Why |
|---|---|---|
| Java | 17+ | The language. Spring Boot 3.x requires it. |
| PostgreSQL | 14+ | Our database. |
| Maven Wrapper | bundled (`./mvnw`) | Downloads Maven for you. No global install needed. |

---

## Setup steps

### 1. Create the database
```bash
createdb lms-project
```

The backend will create all tables automatically on first run via Hibernate (`ddl-auto: update`).

### 2. Copy the env template and fill in your real values
```bash
cp .env.example .env
```

Edit `.env`:
- `DB_PASSWORD` — your Postgres password.
- `JWT_SECRET` — generate fresh:
  ```bash
  openssl rand -base64 32
  ```

The other values have sensible defaults.

### 3. Run

```bash
./mvnw spring-boot:run
```

That's it — no shell setup needed. The backend uses [`spring-dotenv`](https://github.com/paulschwarz/spring-dotenv) (declared in `pom.xml`) which reads `apps/backend/.env` automatically at application startup and exposes the keys to Spring's `${VAR}` placeholder resolution.

**Important:** spring-dotenv looks for `.env` in the **working directory** — i.e., wherever you invoke `./mvnw`. Always run Maven commands from inside `apps/backend/`, not from the repo root.

**Precedence** (highest wins):
1. Real OS environment variables (`export DB_PASSWORD=...` in your shell, or production-platform injections)
2. Values in `apps/backend/.env`
3. Defaults in `application.yml` (the `:default` part of `${VAR:default}`)

This matters in production: deployment platforms inject env vars directly. No `.env` file is shipped. The same code works.

The first run downloads ~200MB of Maven dependencies (1-2 minutes). Subsequent runs start in ~5 seconds.

Watch for:
```
Started LmsBackendApplication in 4.2s
```

API is live at `http://localhost:8081`.

### 5. Smoke test
```bash
curl http://localhost:8081/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@lms.com","password":"admin123"}'
```

Expected: `{"token":"eyJ..."}`. If you get this, you're good.

---

## Seeded accounts

| Email | Password | Role |
|---|---|---|
| `admin@lms.com` | `admin123` | `ADMIN` |
| `librarian@lms.com` | `lib123` | `LIBRARIAN` |
| `student@lms.com` | `student123` | `STUDENT` |
| `jane@lms.com` | `jane123` | `STUDENT` |

The data initializer (`com.lms.config.DataInitializer`) seeds these plus 5 authors, 5 categories, 8 books, and a mix of loan records (active / returned / overdue) so the dashboards and fine system have data to work with on first boot.

---

## Useful commands

```bash
./mvnw spring-boot:run         # Start the backend
./mvnw test                    # Run all tests (should be 17 green)
./mvnw clean package           # Build a deployable .jar
./mvnw -DskipTests compile     # Just compile (faster than full test run)
```

The Maven Wrapper caches downloaded dependencies under `~/.m2/repository`. Wipe that folder if dependencies get corrupted.

---

## Database operations

```bash
psql -d lms-project            # connect interactively
\dt                            # list tables
\d users                       # describe a specific table
\q                             # quit
```

Wipe everything to start fresh (you'll lose all data):
```bash
dropdb lms-project && createdb lms-project
```

---

## Next steps

- **[02-SPRING_BOOT_CRASH_COURSE.md](./02-SPRING_BOOT_CRASH_COURSE.md)** — required if you're new to Spring Boot.
- **[03-CURRENT_IMPLEMENTATION.md](./03-CURRENT_IMPLEMENTATION.md)** — what's built so far.
- **[04-OVERDUE_FINES.md](./04-OVERDUE_FINES.md)** — deep dive on the fine system.
- **[../../../docs/API_REFERENCE.md](../../../docs/API_REFERENCE.md)** — every endpoint.
- **[../../../docs/DEVELOPMENT_GUIDE.md#troubleshooting](../../../docs/DEVELOPMENT_GUIDE.md#troubleshooting)** — troubleshooting guide if anything went wrong.
