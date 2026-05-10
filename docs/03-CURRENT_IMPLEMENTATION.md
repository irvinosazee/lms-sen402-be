# Current Implementation - Backend

## 1. Authentication & Security
- **JWT Auth:** Fully implemented using `HS256` signing.
- **Filter Chain:** `JwtAuthenticationFilter` handles token extraction and validation.
- **Resilience:** Filter now ignores invalid/expired tokens for public endpoints (login/register) to prevent crashes.
- **RBAC:** Enforced at the Controller level using `@PreAuthorize`.

## 2. Core Entities
- **Users:** Supporting `ADMIN`, `LIBRARIAN`, `STUDENT`.
- **Books:** Tracks `availableCopies` vs `totalCopies`.
- **Loans:** Tracks `borrowDate`, `dueDate`, and `status`.

## 3. Advanced Features
- **Statistics Engine:** High-performance JPA queries for the role-based dashboard.
- **User Directory:** Restricted endpoint for managing the member list.
- **Search:** Dynamic book search by title/ISBN.

## 4. Error Handling
- **Standardized Response:** All exceptions return a consistent JSON schema via `GlobalExceptionHandler`.
- **Auth Errors:** Bad credentials correctly return `401 Unauthorized`.

## 5. Persistence
- **PostgreSQL:** Production-grade schema with Hibernate auto-update enabled for development.
- **Data Initializer:** Seed logic for default users and sample books.
