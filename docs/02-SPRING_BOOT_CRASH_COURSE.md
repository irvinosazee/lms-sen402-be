# Spring Boot Crash Course for Beginners

Welcome to the backend of the Library Management System (LMS)! This application is built using Java and the Spring Boot framework.

If you've never used Spring Boot before, don't worry! This guide is designed to explain the core concepts, the syntax, and the architecture you'll encounter in this project.

---

## 1. What is Spring Boot?

Spring Boot is an extension of the Spring framework that provides "auto-configuration." It provides the scaffolding and plumbing, so you can focus strictly on the business logic.

---

## 2. The Core Concept: Inversion of Control (IoC) & Dependency Injection (DI)

### The Problem:
Normally, if a `Controller` needs a `Service` to do some work, you would write:
`BookService service = new BookService();` 
This tightly couples your code, making it hard to test.

### The Spring Solution:
Spring acts as a giant "Container." When the application starts, Spring creates all your objects (called **Beans**) and wires them together via **Constructor Injection**.

### How we do it (Manual Constructor Injection):
In this project, we avoid "magic" like Lombok and use standard Java for maximum compatibility.
```java
@Service // Tells Spring: "Make this a Bean"
public class BookService {
    // ...
}

@RestController // Tells Spring: "This is a Bean that handles web requests"
public class BookController {

    private final BookService bookService; 

    // We manually write the constructor. Spring sees this and injects the Service!
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }
}
```

---

## 3. The Layered Architecture

Our backend strictly follows: **Controller → Service → Repository**.

### Layer 1: The Repository (`com.lms.repositories`)
**Job:** Talk to the Database.
Extend `JpaRepository`. Spring automatically implements CRUD queries for you!
```java
public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);
}
```

### Layer 2: The Service (`com.lms.services`)
**Job:** Business Logic.
This is where the "thinking" happens. Services coordinate with Repositories.

### Layer 3: The Controller (`com.lms.controllers`)
**Job:** Handle HTTP Requests and Responses.
Controllers are responsible for receiving JSON, calling a Service, and returning a JSON response.

---

## 4. DTOs vs Entities (Standard POJOs)

### Entities (`com.lms.entities`)
Java classes mapped directly to database tables using `@Entity`.

### DTOs (Data Transfer Objects) (`com.lms.dtos`)
Simple objects used to pass data to the React frontend. 

**The Rule:** *Never expose Entities directly from Controllers.* 
In this project, all Entities and DTOs use **Standard Java POJO patterns**:
1.  **Private Fields**
2.  **Explicit Constructors**
3.  **Explicit Getters and Setters** (No Lombok `@Data`)

Example:
```java
public class User {
    private String email;
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
```

---

## 5. Security & JWT

Our backend is secured using **Spring Security** and **JSON Web Tokens (JWT)**.
- **Login:** Returns a JWT.
- **Auth:** Client sends JWT in the `Authorization: Bearer <token>` header.
- **Filter:** `JwtAuthenticationFilter` validates the token on every request.

## Conclusion
By keeping your code explicit (manual constructors and standard getters/setters), the codebase remains easy to debug and compatible with all Java environments.
