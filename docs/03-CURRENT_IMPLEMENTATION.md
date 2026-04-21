# Current Backend Implementation Status

This document tracks the implemented features and the current state of the backend.

---

## 1. Project Foundation
- **Framework:** Spring Boot 3.3.5 (Java 17+)
- **Database:** PostgreSQL (with H2 capability)
- **Tooling:** Maven (Lombok removed for better environment compatibility)

## 2. Core Modules & Entities

### Authentication & Authorization
- **Implementation:** Standard Spring Security with manual dependency injection.
- **Security Mechanism:** JWT implementation via `JwtService`.
- **Entities:** `User` (with roles `ADMIN`, `LIBRARIAN`, `STUDENT`).
- **Standard Pattern:** All entities and DTOs use **explicit constructors, getters, and setters**.

### Library Catalog
- **Logic:** `BookService` handles CRUD operations.
- **Filtering:** Implemented `BookSpecification` for multi-field searching (Title, Author, Category, ISBN).
- **Pagination:** All catalog endpoints return `Page<BookResponseDTO>` for efficient UI rendering.

### Loan System
- **Transactional Integrity:** `LoanService` ensures that book availability is updated atomically with loan creation.
- **Endpoints:** `/api/v1/loans/borrow`, `/api/v1/loans/return`, `/api/v1/loans/my`.

## 3. Infrastructure
- **Global Exception Handling:** Centralized JSON error responses.
- **Data Seeding:** `DataInitializer` seeds an Admin and a Student account at startup.
