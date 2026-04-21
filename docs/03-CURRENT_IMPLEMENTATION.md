# Current Backend Implementation Status

This document tracks the implemented features, architecture, and current state of the Library Management System (LMS) backend. 

*Note: As per our architectural rules, developers must update this document when modifying or adding backend capabilities.*

---

## 1. Project Foundation
- **Framework:** Spring Boot 3.x (Java 17+)
- **Database:** PostgreSQL (Production) / H2 (Development capability configured via application.yml)
- **Tooling:** Maven, Lombok

## 2. Core Modules & Entities

### Authentication & Authorization (`User` Entity)
- **Implementation:** Custom `UserDetails` and `UserDetailsService` hooked into Spring Security.
- **Security Mechanism:** Stateless JSON Web Token (JWT) implementation via `JwtService` and `JwtAuthenticationFilter`.
- **Roles:** `ADMIN`, `LIBRARIAN`, `STUDENT`.
- **Endpoints:**
  - `POST /api/v1/auth/register` (Registers a new user and returns JWT)
  - `POST /api/v1/auth/login` (Authenticates user and returns JWT)
- **Token Claims:** The JWT payload contains the user's email (subject), ID, role, first name, and last name to facilitate frontend state initialization.

### Library Catalog (`Book`, `Author`, `Category` Entities)
- **Data Model:** Normalized. A `Book` belongs to one `Author` and one `Category`.
- **Logic:** `BookService` handles CRUD operations mapping `BookRequestDTO` to the underlying entities and converting the output to `BookResponseDTO`.
- **Endpoints:**
  - `GET /api/v1/books` (Public) - Supports Pagination (`page`, `size`, `sort`) and Search filtering (`query` matching title, ISBN, author, or category via `BookSpecification`).
  - `GET /api/v1/books/{id}` (Public)
  - `POST /api/v1/books` (Protected: `ADMIN`, `LIBRARIAN`)
  - `PUT /api/v1/books/{id}` (Protected: `ADMIN`, `LIBRARIAN`)
  - `DELETE /api/v1/books/{id}` (Protected: `ADMIN`, `LIBRARIAN`)
  - `GET /api/v1/authors` (Public)
  - `GET /api/v1/categories` (Public)

### Loan System (`Loan` Entity) - **CRITICAL FEATURE**
- **Data Model:** Links `User` and `Book`. Tracks `borrowDate`, `dueDate`, `returnDate`, and `LoanStatus` (`BORROWED`, `RETURNED`, `OVERDUE`).
- **Transactional Integrity:** Methods in `LoanService` are marked with `@Transactional` to ensure that decreasing a book's `availableCopies` and creating the `Loan` record happen atomically. Prevents race conditions and negative availability.
- **Endpoints:**
  - `POST /api/v1/loans/borrow` (Protected: `STUDENT`): Borrows a book. Fails if `availableCopies <= 0`.
  - `POST /api/v1/loans/{id}/return` (Protected: Any): Marks the loan as `RETURNED` and increments the book's `availableCopies`.
  - `GET /api/v1/loans/my` (Protected: Any): Retrieves the currently authenticated user's active and past loans.
  - `GET /api/v1/loans` (Protected: `ADMIN`, `LIBRARIAN`): Retrieves all loans across the system.

## 3. Infrastructure & Error Handling

### Global Exception Handling
- **Implementation:** `@ControllerAdvice` via `GlobalExceptionHandler.java`.
- **Coverage:** Catches `MethodArgumentNotValidException` (Validation errors), `ResourceNotFoundException`, and generic `Exception`.
- **Format:** All errors return a structured JSON response containing:
  ```json
  {
    "timestamp": "2026-04-21T...",
    "status": 404,
    "error": "Not Found",
    "message": "Book not found",
    "path": "/api/v1/books/99"
  }
  ```

### Cross-Origin Resource Sharing (CORS)
- **Implementation:** `WebConfig` class implementing `WebMvcConfigurer`.
- **Configuration:** Allows all methods (`GET`, `POST`, `PUT`, `DELETE`, etc.) and headers from `http://localhost:5173` (Vite's default port), with credentials enabled for secure cookie/token passage.

### Data Seeding
- **Implementation:** `DataInitializer` class implementing `CommandLineRunner`.
- **Functionality:** Injects a default Admin user (`admin@lms.com`), a Student user (`student@lms.com`), and a sample book ("Harry Potter") into the database on application startup if they don't already exist.
