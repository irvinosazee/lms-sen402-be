# LMS Backend - Spring Boot API

This is the core engine of the Library Management System. It handles all business logic, data persistence, and security.

## 🔧 Core Tech Stack
- **Framework:** Spring Boot 3.4
- **Security:** Spring Security + JWT
- **Persistence:** Spring Data JPA + Hibernate
- **Database:** PostgreSQL (with H2 capability for dev)
- **Build Tool:** Maven

## 📂 Internal Guides
We have provided extensive documentation for both beginners and advanced developers:
1. **[01-GETTING-STARTED.md](./docs/01-GETTING-STARTED.md):** How to configure the DB and run the app.
2. **[02-SPRING-BOOT-CRASH-COURSE.md](./docs/02-SPRING_BOOT_CRASH_COURSE.md):** A full guide to Spring Boot syntax and patterns (Controller, Service, Repository).
3. **[03-CURRENT-IMPLEMENTATION.md](./docs/03-CURRENT-IMPLEMENTATION.md):** Details on all active endpoints, schemas, and logic.

## 🚦 How to Run
```bash
./mvnw spring-boot:run
```
The API will be available at `http://localhost:8081`.

## 🛡 Security
All endpoints (except `/api/v1/auth/**`) require a valid JWT passed in the `Authorization: Bearer <token>` header.
