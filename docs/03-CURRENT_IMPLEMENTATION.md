# Backend — Current Implementation

Snapshot of what's running in the backend as of 2026-05-11. For deeper dives on individual subsystems, see the linked docs.

---

## 1. Authentication & Security
- **JWT (HS256)** signing. Secret read from `JWT_SECRET` env var (no hardcoded fallback).
- **`JwtAuthenticationFilter`** extracts and validates the token on every request. Tolerates missing/invalid tokens on `/auth/**` so login can succeed.
- **BCrypt** for password hashing, strength factor 10.
- **RBAC** at three layers: URL patterns in `SecurityConfig`, `@PreAuthorize` on controller methods, ownership checks in services.
- **Stateless** — no server-side session store. Token is the session.
- See: [`../../../docs/SECURITY_ARCHITECTURE.md`](../../../docs/SECURITY_ARCHITECTURE.md).

## 2. Core Entities
- **`User`** — id, email (unique), BCrypt password, firstName, lastName, role (`ADMIN | LIBRARIAN | STUDENT`), timestamps.
- **`Author`** — id, name, bio.
- **`Category`** — id, name, description.
- **`Book`** — id, title, isbn (unique), totalCopies, availableCopies, **`@Version`** field for optimistic locking, FK author, FK category, timestamps.
- **`Loan`** — id, FK book, FK user, borrowDate, dueDate, returnDate (nullable), status (`BORROWED | RETURNED`), **`finePaid`** boolean default false, **`finePaidAt`** timestamp.
- **`LoanStatus`** and **`Role`** are Java enums stored as strings (`@Enumerated(EnumType.STRING)`).

## 3. Business Rules in the Service Layer

### `BookService`
- Full CRUD. On update, if `totalCopies` changes, `availableCopies` is adjusted by the same delta so checked-out books stay checked out.
- Search via JPA `Specification` (`BookSpecification.search`) — substring match across title, author name, category name, ISBN.

### `LoanService`
- **`borrowBook(bookId)`** — 14-day due date. Rejects if the borrower has any unsettled outstanding fine (the borrow-gate). Rejects if `availableCopies <= 0`. Decrements `availableCopies` and creates the loan in a single transaction.
- **`returnBook(loanId)`** — Service-layer ownership check: only the borrower, ADMIN, or LIBRARIAN may return. Sets `returnDate`, flips status to `RETURNED`, increments `availableCopies`. Rejects double-return.
- **`settleFine(loanId)`** — ADMIN/LIBRARIAN only. Three rejection cases: loan still borrowed, fine already paid, no fine due. Sets `finePaid = true`, `finePaidAt = now()`.

### `AuthService`
- Login: verify BCrypt password, issue JWT with embedded role + name + id claims.
- Register: hash password, persist user, immediately issue JWT.

### `StatsService`
- Role-aware aggregates:
  - **ADMIN/LIBRARIAN:** `totalBooks`, `totalStudents`, `activeLoans`, `overdueCount`, `outstandingFinesTotal`.
  - **STUDENT:** `myActiveLoans`, `totalBorrowed`, `overdueCount`, `myOutstandingFines`.
- Recent activity feed: 5 most recent loans (system-wide for staff, personal for students).

### `LoanFines` (utility)
- Pure static methods. `daysOverdue(loan)` and `fineAccrued(loan, ratePerDay)`. Computed on read; not stored. See [`04-OVERDUE_FINES.md`](./04-OVERDUE_FINES.md).

## 4. Controllers (REST API)
- `AuthController` — `/auth/login`, `/auth/register`.
- `BookController` — full CRUD with pagination and search.
- `AuthorController`, `CategoryController` — CRUD.
- `LoanController` — `/loans/borrow`, `/loans/{id}/return`, `/loans/{id}/settle-fine`, `/loans/my`, `/loans`.
- `StatsController` — `/stats` (role-aware response).
- `UserController` — `/users` (staff-only directory).

Every endpoint is documented in [`../../../docs/API_REFERENCE.md`](../../../docs/API_REFERENCE.md) with curl examples.

## 5. Error Handling
- `GlobalExceptionHandler` (`@ControllerAdvice`) catches every custom exception and translates to a uniform `ErrorResponse` JSON:
  - `BadRequestException` → 400
  - `ResourceNotFoundException` → 404
  - `ForbiddenException` / `AccessDeniedException` → 403
  - `BadCredentialsException` / `AuthenticationException` → 401
  - `ConflictException` / `OptimisticLockingFailureException` → 409
  - Bean Validation failure → 400 with field-error list
  - Anything else → 500
- See [`../../../docs/API_REFERENCE.md#error-format`](../../../docs/API_REFERENCE.md#error-format) for the response shape.

## 6. Concurrency
- **Optimistic locking** on `Book` via `@Version`. Prevents two concurrent borrows from oversubscribing the last copy. Conflict surfaces as 409.
- All write operations are `@Transactional`.

## 7. Configuration
- All sensitive values come from environment variables (`.env` loaded via `set -a; source .env; set +a`).
- `application.yml` has no real secrets — only `${VAR}` placeholders.
- Config keys:
  - `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
  - `SERVER_PORT`
  - `JWT_SECRET`, `JWT_EXPIRATION_MS`
  - `LOAN_FINE_PER_DAY`

## 8. Tests
- **17 unit tests, all green.** Run with `./mvnw test`.
- `LoanServiceTest` — borrow success, no-copies, return success, already-returned, fine settlement success, borrow blocked by outstanding fine.
- `BookServiceTest` — CRUD coverage.
- `LoanFinesTest` — overdue math edge cases (not overdue, borrowed-and-overdue, returned-late-frozen, returned-on-time).
- `LmsBackendApplicationTests` — full Spring context loads.

## 9. Persistence
- PostgreSQL with `spring.jpa.hibernate.ddl-auto: update` for dev. Schema auto-migrates on entity changes (with caveats — see [`../../../docs/DATABASE_SCHEMA.md#schema-evolution`](../../../docs/DATABASE_SCHEMA.md#schema-evolution)).
- `DataInitializer` (CommandLineRunner) seeds default users, authors, categories, books, and loans on first run against an empty database.

## 10. Known limitations
- No Swagger/OpenAPI doc generation (manual API reference in `docs/API_REFERENCE.md` instead).
- No integration tests for race conditions (covered by `@Version` mechanism, not by a stressed concurrency test).
- No production profile (`application-prod.yml`) — would tighten log levels, disable `show-sql`, etc.
- No Flyway/Liquibase — `ddl-auto: update` is dev-only safe.
- No email / SMS notifications.

See the root `CHECKLIST.md` for the full feature inventory.
