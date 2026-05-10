# Spring Boot Crash Course

You have never touched Java or Spring Boot. By the end of this doc you'll understand the backend of this project. Every annotation and pattern is explained with a real example from this codebase — no toy snippets.

If you have used Spring Boot before, skim §1-§3 and read §4 onward.

---

## 1. What you need to know about Java first

### 1.1 The Java ecosystem

- **JDK (Java Development Kit)** — the tools to *compile* Java. Includes the compiler `javac`, the package builder, etc.
- **JRE (Java Runtime Environment)** — the tools to *run* compiled Java. Mostly the **JVM** (Java Virtual Machine).
- **Maven** — a build tool that resolves dependencies (libraries) and packages your code. Reads `pom.xml`. Equivalent to `package.json` + `npm` in JavaScript.
- **`./mvnw`** — the **Maven Wrapper** script. Lets you run Maven without installing it globally — it downloads the right version automatically.

You only need the JDK installed (17+). Everything else comes from `./mvnw`.

### 1.2 Files and packages

A `.java` file declares one **public class**. The file name must match the class name (`Book.java` contains `public class Book`).

A **package** is just a folder structure mapped to a dotted name:

```
src/main/java/com/lms/services/LoanService.java
                ↑↑↑↑↑↑↑↑↑↑↑↑↑    ↑↑↑↑↑↑↑↑↑↑↑
                package           class
```

The file header declares the package, and other files `import` from it:

```java
package com.lms.services;          // I am in package com.lms.services

import com.lms.entities.Loan;       // pull in Loan from another package
```

This project uses the root package `com.lms`. Subpackages are organized by layer (`controllers`, `services`, `repositories`, `entities`, `dtos`, `exceptions`, `security`, `config`).

### 1.3 Annotations

`@SomeAnnotation` is metadata attached to a class, method, or field. Annotations don't *do* anything on their own — but **frameworks read them** at startup or runtime to decide how to wire your code. Spring Boot is heavily annotation-driven.

You'll see annotations like `@Service`, `@RestController`, `@Autowired`, `@Transactional`, `@PreAuthorize`, `@Entity`, `@Column`, etc. Each one is explained below in context.

---

## 2. What Spring Boot does for you

Spring Boot is a framework that handles the **plumbing** so you can focus on business logic.

Without it you'd have to:
- Configure an HTTP server (Tomcat) manually.
- Wire up a connection pool to Postgres.
- Convert JSON ↔ Java objects.
- Write a security filter chain.
- Manage object lifecycles (when to construct, when to destroy).

With Spring Boot, you:
- Add a dependency.
- Annotate a class.
- It works.

Run `./mvnw spring-boot:run` and Spring Boot:
1. Starts an embedded Tomcat server on port 8081.
2. Scans your packages for classes annotated with `@Component`, `@Service`, `@RestController`, etc. and instantiates them ("beans").
3. Wires them together by **dependency injection** (next section).
4. Reads `application.yml` for config.
5. Connects to Postgres (because we declared `spring-boot-starter-data-jpa` and a `postgresql` driver in `pom.xml`).
6. Runs any `CommandLineRunner` beans (we have one: `DataInitializer`, which seeds users and books).
7. Listens for HTTP requests and routes them to your `@RestController` methods.

---

## 3. Dependency Injection (DI) — the single most important concept

### The problem

Imagine `BookController` needs `BookService` to do its work. The naive approach:

```java
public class BookController {
    private BookService service = new BookService();  // ← tightly coupled
}
```

Why this is bad:
- `BookController` *creates* its own `BookService`. You can't swap in a mock for tests.
- `BookService` might need a `BookRepository`. So now you need `new BookService(new BookRepository(...))` inside `BookController`. The construction graph explodes.

### Spring's solution: Inversion of Control

You **don't construct your own dependencies**. You declare what you need, and Spring constructs and hands them to you.

```java
@RestController                              // "I am a web controller bean"
public class BookController {
    private final BookService service;       // What I need

    public BookController(BookService service) {   // Constructor — Spring sees this
        this.service = service;                    //  and injects a BookService bean
    }
}
```

At startup:
1. Spring sees `@Service` on `BookService` and constructs one instance.
2. Spring sees `@RestController` on `BookController` and tries to construct one.
3. `BookController`'s constructor needs a `BookService`. Spring already has one. It passes it in.
4. Spring keeps both instances in its **application context** (a giant `Map<Class, Object>`).

In tests, you skip the Spring context entirely and pass mocks to the constructor directly:

```java
BookService mockService = Mockito.mock(BookService.class);
BookController controller = new BookController(mockService);
```

### Real example from this project

```java
// apps/backend/src/main/java/com/lms/services/LoanService.java
@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final long finePerDay;

    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       UserRepository userRepository,
                       @Value("${app.loan.fine-per-day-naira:100}") long finePerDay) {
        if (finePerDay < 0) {
            throw new IllegalStateException("app.loan.fine-per-day-naira must be >= 0");
        }
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.finePerDay = finePerDay;
    }
    // ...
}
```

Three things to notice:

1. **All dependencies are constructor parameters.** No setters, no field injection. This is the modern idiom.
2. **`final` fields.** Once assigned in the constructor, they can never change. Enforces immutability of dependencies.
3. **`@Value("${app.loan.fine-per-day-naira:100}")`** — Spring resolves config values the same way it resolves bean dependencies. The `:100` is a default if the env var is unset.

---

## 4. The layered architecture: Controller → Service → Repository

Every request flows through three layers:

```
HTTP request                                                      JSON response
     ↓                                                                   ↑
┌─────────────────┐    ┌─────────────────┐    ┌──────────────────┐    │
│   Controller    │ →  │     Service     │ →  │    Repository    │ → DB
│                 │    │                 │    │                  │
│ HTTP routing,   │    │ Business logic, │    │ Database queries │
│ request/response│    │ validation,     │    │ via JPA/Hibernate│
│ shape           │    │ orchestration   │    │                  │
└─────────────────┘    └─────────────────┘    └──────────────────┘
```

### 4.1 Repository layer (`com.lms.repositories`)

The repository's only job is to talk to the database. You don't write a class — you write an **interface** that extends `JpaRepository`, and Spring Data JPA generates the implementation at runtime.

```java
// apps/backend/src/main/java/com/lms/repositories/LoanRepository.java
public interface LoanRepository extends JpaRepository<Loan, Long> {
    // 1. Derived queries — Spring parses the method name
    List<Loan> findByUser(User user);
    List<Loan> findByUserAndFinePaidFalse(User user);

    // 2. Custom JPQL when method-name derivation is too limited
    @Query("SELECT count(l) FROM Loan l WHERE l.status = 'BORROWED' AND l.dueDate < :now")
    long countOverdue(LocalDateTime now);
}
```

What's happening:

- `JpaRepository<Loan, Long>` means "I manage `Loan` entities, whose primary key is `Long`". You get `save`, `findById`, `findAll`, `deleteById`, `count`, etc. for free.
- **Derived queries:** Spring parses `findByUserAndFinePaidFalse` and generates `SELECT * FROM loans WHERE user_id = ? AND fine_paid = false`. Method-name keywords: `findBy`, `And`, `Or`, `True`, `False`, `In`, `Like`, `Between`, etc.
- **`@Query`** with JPQL (Java Persistence Query Language) — looks like SQL but operates on entities, not tables. `Loan l` is an entity alias; `l.dueDate` is the entity field, not the column name. Spring/Hibernate translate to SQL at runtime.

You never `new` a repository. Spring constructs the implementation. You inject it.

### 4.2 Service layer (`com.lms.services`)

Where business logic lives. Services orchestrate repositories.

```java
// apps/backend/src/main/java/com/lms/services/LoanService.java
@Transactional
public LoanResponseDTO borrowBook(Long bookId) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

    var unpaid = loanRepository.findByUserAndFinePaidFalse(user);
    boolean hasOutstandingFine = unpaid.stream()
            .anyMatch(l -> LoanFines.fineAccrued(l, finePerDay) > 0);
    if (hasOutstandingFine) {
        throw new BadRequestException("Settle outstanding fines before borrowing again");
    }

    if (book.getAvailableCopies() <= 0) {
        throw new BadRequestException("No copies available for borrowing");
    }

    Loan loan = new Loan(book, user, LocalDateTime.now(), LocalDateTime.now().plusDays(14), LoanStatus.BORROWED);

    book.setAvailableCopies(book.getAvailableCopies() - 1);
    bookRepository.save(book);

    Loan savedLoan = loanRepository.save(loan);
    return mapToResponse(savedLoan);
}
```

Notice:

- **`@Transactional`** — wraps the method in a database transaction. Either every change succeeds, or every change rolls back. If the second `save` fails, the first one is undone.
- **`SecurityContextHolder`** — Spring Security's way to find the currently authenticated user. The `JwtAuthenticationFilter` put them there.
- **Exception types** — `ResourceNotFoundException`, `BadRequestException` are custom classes (in `com.lms.exceptions`). The `@ControllerAdvice` `GlobalExceptionHandler` catches them and turns them into proper HTTP responses (404, 400 respectively).
- **`mapToResponse`** — converts an internal `Loan` entity into a `LoanResponseDTO` (Data Transfer Object). We **never** return entities directly from the API — DTOs decouple the wire format from the database schema.

### 4.3 Controller layer (`com.lms.controllers`)

The thinnest layer. It maps HTTP routes to service calls.

```java
// apps/backend/src/main/java/com/lms/controllers/LoanController.java
@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/borrow")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LoanResponseDTO> borrowBook(@Valid @RequestBody LoanRequestDTO request) {
        return ResponseEntity.ok(loanService.borrowBook(request.getBookId()));
    }

    @PostMapping("/{loanId}/settle-fine")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<LoanResponseDTO> settleFine(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.settleFine(loanId));
    }
}
```

Annotations:

| Annotation | What it does |
|---|---|
| `@RestController` | Combines `@Controller` + `@ResponseBody`. Every method returns JSON, not an HTML template. |
| `@RequestMapping("/api/v1/loans")` | Prefixes all routes in this class. |
| `@PostMapping("/borrow")` | Handles `POST /api/v1/loans/borrow`. There's also `@GetMapping`, `@PutMapping`, `@DeleteMapping`. |
| `@PreAuthorize("hasRole('STUDENT')")` | Spring Security checks the user's role before invoking the method. Returns 403 if denied. |
| `@PathVariable Long loanId` | Pull `{loanId}` from the URL path. |
| `@RequestBody` | Deserialize the request JSON into the parameter object. Jackson handles the JSON ↔ POJO mapping. |
| `@Valid` | Trigger Bean Validation (`@NotNull`, `@Email`, etc.) on the request DTO. |
| `ResponseEntity<...>` | Wrap the response with HTTP status, headers. `ResponseEntity.ok(x)` sets status to 200. |

The controller doesn't *do* anything beyond input parsing, calling the service, and returning the result.

---

## 5. JPA Entities — Java objects ↔ database tables

An **entity** is a Java class that JPA maps to a database table. You annotate the class with `@Entity` and Hibernate generates SQL to create/insert/update/delete rows.

```java
// apps/backend/src/main/java/com/lms/entities/Loan.java
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime borrowDate;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    private LocalDateTime returnDate;     // nullable by default

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus status;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean finePaid = false;

    private LocalDateTime finePaidAt;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    // ...getters and setters...
}
```

| Annotation | Meaning |
|---|---|
| `@Entity` | This class is a database table. |
| `@Table(name = "loans")` | Override the default table name (which would be `Loan`). |
| `@Id` | Marks the primary key. |
| `@GeneratedValue(strategy = IDENTITY)` | Auto-increment from the database. |
| `@Column(nullable = false)` | This column is `NOT NULL`. |
| `@Column(columnDefinition = "BOOLEAN DEFAULT FALSE")` | Override how Hibernate writes the column DDL. Used here so existing rows backfill `false` when the column is added. |
| `@ManyToOne` | Many loans belong to one book. Creates a foreign key. |
| `@JoinColumn(name = "book_id")` | The FK column is called `book_id`. |
| `fetch = FetchType.LAZY` | Don't load the related `Book` until you call `loan.getBook()`. Critical for performance. |
| `@Enumerated(EnumType.STRING)` | Store the enum as its string name, not the ordinal index. Robust against enum reordering. |
| `@CreationTimestamp` | Hibernate sets this on first save. |
| `@UpdateTimestamp` | Hibernate updates this on every save. |

### The `@Version` annotation (optimistic locking)

```java
// apps/backend/src/main/java/com/lms/entities/Book.java
@Version
@Column(columnDefinition = "BIGINT DEFAULT 0")
private Long version;
```

`@Version` tells Hibernate: every UPDATE to this row must include `WHERE version = <current value>` and `SET version = <current value> + 1`. If two transactions read `version = 5` and both try to update, only the first succeeds — the second sees zero rows affected and Hibernate throws `OptimisticLockingFailureException`.

This prevents two students from racing to grab the last available copy of a book.

---

## 6. DTOs vs Entities — never expose entities

Entities (`Loan`, `Book`, etc.) are tied to the database. They have JPA annotations, lazy-loaded relationships that explode when serialized, and may contain sensitive fields (e.g., `User.password`).

**DTOs** (Data Transfer Objects, in `com.lms.dtos`) are plain Java objects with **just the shape we want on the wire**. The service layer converts entities → DTOs before returning anything to the controller.

```java
// apps/backend/src/main/java/com/lms/dtos/LoanResponseDTO.java
public class LoanResponseDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long userId;
    private String userEmail;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private LoanStatus status;
    private long daysOverdue;
    private long fineAccrued;
    private long fineOutstanding;
    private boolean finePaid;
    // ... getters and setters ...
}
```

Notice:
- No JPA annotations.
- Includes **computed** fields (`daysOverdue`, `fineAccrued`, `fineOutstanding`) that aren't in the database.
- Includes the related book's title and the user's email directly — flattened, no nested object navigation required by the client.

Jackson (the JSON library Spring uses) serializes this to JSON automatically. You don't write `toJson()` methods.

---

## 7. Exception handling — `@ControllerAdvice`

Throw exceptions freely from services. A single `@ControllerAdvice` class catches them all and translates them into HTTP responses.

```java
// apps/backend/src/main/java/com/lms/exceptions/GlobalExceptionHandler.java
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
            LocalDateTime.now(),
            HttpStatus.BAD_REQUEST.value(),
            "Bad Request",
            ex.getMessage(),
            request.getRequestURI()
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // ...handlers for ResourceNotFoundException → 404,
    //  ForbiddenException → 403, ConflictException → 409, etc.
}
```

This means:
- Services don't think about HTTP. They just throw `BadRequestException("No copies available")`.
- The error response shape is consistent across every endpoint.

---

## 8. Spring Security & JWT

Configured in `apps/backend/src/main/java/com/lms/security/SecurityConfig.java`:

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity                 // ← lets us use @PreAuthorize on methods
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()      // login/register: public
                .anyRequest().authenticated()                          // everything else: needs a token
            )
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
```

The flow:

1. Every request hits `JwtAuthenticationFilter` first.
2. It extracts the `Authorization: Bearer <token>` header.
3. It validates the token using `JwtService` and the `JWT_SECRET`.
4. If valid, it creates an `Authentication` object containing the user's email and role and puts it in `SecurityContextHolder`.
5. The request continues. `@PreAuthorize("hasRole('ADMIN')")` checks the `SecurityContextHolder` and allows or rejects.

Stateless = no session storage on the server. The token is the session.

Read more in `docs/SECURITY_ARCHITECTURE.md`.

---

## 9. Configuration: `application.yml` + `@Value`

`apps/backend/src/main/resources/application.yml` is the central config file:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:postgresql://localhost:5432/lms-project}
    username: ${DB_USERNAME:postgres}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: update

app:
  jwt:
    secret: ${JWT_SECRET}
    expiration-ms: ${JWT_EXPIRATION_MS:604800000}
  loan:
    fine-per-day-naira: ${LOAN_FINE_PER_DAY:100}
```

- `${VAR}` reads from the OS environment. No default → required.
- `${VAR:default}` reads from env, falls back to `default` if unset.
- **No real secrets in YAML.** Anything sensitive comes through env.

Inject these into your beans with `@Value`:

```java
public LoanService(LoanRepository ..., @Value("${app.loan.fine-per-day-naira:100}") long finePerDay) {
    this.finePerDay = finePerDay;
}
```

---

## 10. Testing — JUnit 5 + Mockito

Unit tests live in `apps/backend/src/test/java/com/lms/`. They use **Mockito** to mock repositories so tests don't hit the database.

```java
// apps/backend/src/test/java/com/lms/services/LoanServiceTest.java
class LoanServiceTest {

    @Mock private LoanRepository loanRepository;
    @Mock private BookRepository bookRepository;
    @Mock private UserRepository userRepository;
    private LoanService loanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        loanService = new LoanService(loanRepository, bookRepository, userRepository, 100L);
        // ...arrange test fixtures...
    }

    @Test
    void borrowBook_BlockedByOutstandingFine() {
        Loan oldOverdue = new Loan(book, user,
                LocalDateTime.now().minusDays(20),
                LocalDateTime.now().minusDays(6),
                LoanStatus.RETURNED);
        oldOverdue.setReturnDate(LocalDateTime.now().minusDays(2));
        oldOverdue.setFinePaid(false);

        when(userRepository.findByEmail("student@lms.com")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(loanRepository.findByUserAndFinePaidFalse(user)).thenReturn(List.of(oldOverdue));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> loanService.borrowBook(1L));
        assertEquals("Settle outstanding fines before borrowing again", ex.getMessage());
    }
}
```

Steps in every test:
1. **Arrange** — set up mocks with `when(...).thenReturn(...)`.
2. **Act** — call the method under test.
3. **Assert** — `assertEquals`, `assertThrows`, or `verify(mock).someCall()`.

Run all tests:
```bash
./mvnw test
```

---

## 11. Common gotchas

### Lazy loading outside a transaction
If your service method isn't `@Transactional` and you call `loan.getBook().getTitle()`, you'll get `LazyInitializationException`. Either annotate the method or fetch eagerly.

### Forgetting `@RequestBody`
```java
@PostMapping("/x")
public X create(MyDto dto) { ... }            // ❌ Spring won't parse JSON; dto fields are all null

@PostMapping("/x")
public X create(@RequestBody MyDto dto) { ... }   // ✓
```

### Forgetting `@Transactional` on a write that touches multiple repos
```java
public LoanResponseDTO borrowBook(Long bookId) {
    book.setAvailableCopies(book.getAvailableCopies() - 1);
    bookRepository.save(book);
    loanRepository.save(loan);   // ← if this fails, the book.save above is NOT rolled back
}
```
Add `@Transactional` so both succeed or both fail.

### Using `equals` to compare enums
Use `==`. Enums are singletons.
```java
if (loan.getStatus() == LoanStatus.BORROWED) { ... }   // ✓
if (loan.getStatus().equals(LoanStatus.BORROWED)) { ... }   // works but not idiomatic
```

### `Optional` everywhere
JPA methods like `findById` return `Optional<T>` because the row may not exist. Two idiomatic ways to handle:
```java
// Throw if missing
Book book = bookRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

// Default value
Book book = bookRepository.findById(id).orElse(defaultBook);
```

### N+1 queries (the performance footgun)
```java
List<Loan> loans = loanRepository.findAll();
for (Loan loan : loans) {
    System.out.println(loan.getBook().getTitle());   // ← one extra query per loan!
}
```
Fix: write a JPQL `JOIN FETCH` query that loads loans and their books in one go. Not yet needed at our data scale, but file it under "things to fix at production scale."

---

## 12. Where to go next

- **[03-CURRENT_IMPLEMENTATION.md](./03-CURRENT_IMPLEMENTATION.md)** — what features currently exist in this backend.
- **[04-OVERDUE_FINES.md](./04-OVERDUE_FINES.md)** — deep dive on the fine system, the most recent feature.
- **[../../../docs/API_REFERENCE.md](../../../docs/API_REFERENCE.md)** — every endpoint with examples.
- **[../../../docs/DATABASE_SCHEMA.md](../../../docs/DATABASE_SCHEMA.md)** — every table and column.
- **[../../../docs/SECURITY_ARCHITECTURE.md](../../../docs/SECURITY_ARCHITECTURE.md)** — the JWT + env-var story in full.

### External references

- Spring Boot reference: <https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/>
- JPA tutorial: <https://www.baeldung.com/learn-jpa-hibernate>
- Spring Data JPA query derivation: <https://docs.spring.io/spring-data/jpa/reference/jpa/query-methods.html>

If anything in this doc is unclear, that's a doc bug. Open an issue and tag the maintainer.
