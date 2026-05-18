package com.lms.config;

import com.lms.entities.*;
import com.lms.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds demo data on first startup: admin + student users, one author, one category, one book.
 * CommandLineRunner.run is called by Spring Boot after the context is ready.
 */
@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, AuthorRepository authorRepository,
                           CategoryRepository categoryRepository, BookRepository bookRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Starting data initialization...");

        // Each block is idempotent — checks existence first so a re-run doesn't duplicate.
        if (userRepository.findByEmail("admin@lms.com").isEmpty()) {
            log.info("Creating admin user...");
            userRepository.save(new User(
                "admin@lms.com",
                passwordEncoder.encode("admin123"),    // BCrypt — never store plaintext
                "Admin", "User", Role.ADMIN
            ));
        }

        if (userRepository.findByEmail("student@lms.com").isEmpty()) {
            log.info("Creating student user...");
            userRepository.save(new User(
                "student@lms.com",
                passwordEncoder.encode("student123"),
                "John", "Doe", Role.STUDENT
            ));
        }

        // Only seed catalog content if it's completely empty — keep prod-like DBs untouched.
        if (authorRepository.count() == 0) {
            log.info("No authors found, creating sample data...");
            Author author = authorRepository.save(new Author("J.K. Rowling", "Author of Harry Potter"));
            Category category = categoryRepository.save(new Category("Fantasy", "Magical worlds and adventures"));

            bookRepository.save(new Book(
                "Harry Potter and the Sorcerer's Stone",
                "9780590353427",
                10, 10,                                // total + available copies
                author, category
            ));
            log.info("Sample data created successfully.");
        } else {
            log.info("Database already contains data, skipping sample data creation.");
        }
    }
}
