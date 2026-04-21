package com.lms.config;

import com.lms.entities.*;
import com.lms.repositories.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, AuthorRepository authorRepository, CategoryRepository categoryRepository, BookRepository bookRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("Starting data initialization...");
        if (userRepository.findByEmail("admin@lms.com").isEmpty()) {
            log.info("Creating admin user...");
            User admin = new User(
                "admin@lms.com",
                passwordEncoder.encode("admin123"),
                "Admin",
                "User",
                Role.ADMIN
            );
            userRepository.save(admin);
        }

        if (userRepository.findByEmail("student@lms.com").isEmpty()) {
            log.info("Creating student user...");
            User student = new User(
                "student@lms.com",
                passwordEncoder.encode("student123"),
                "John",
                "Doe",
                Role.STUDENT
            );
            userRepository.save(student);
        }

        if (authorRepository.count() == 0) {
            log.info("No authors found, creating sample data...");
            Author author = authorRepository.save(new Author("J.K. Rowling", "Author of Harry Potter"));
            Category category = categoryRepository.save(new Category("Fantasy", "Magical worlds and adventures"));
            
            Book book = new Book(
                "Harry Potter and the Sorcerer's Stone",
                "9780590353427",
                10,
                10,
                author,
                category
            );
            bookRepository.save(book);
            log.info("Sample data created successfully.");
        } else {
            log.info("Database already contains data, skipping sample data creation.");
        }
    }
}
