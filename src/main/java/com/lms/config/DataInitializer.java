package com.lms.config;

import com.lms.entities.*;
import com.lms.repositories.*;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Create Admin
        if (userRepository.findByEmail("admin@lms.com").isEmpty()) {
            User admin = User.builder()
                    .firstName("Admin")
                    .lastName("User")
                    .email("admin@lms.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
        }

        // Create Student
        if (userRepository.findByEmail("student@lms.com").isEmpty()) {
            User student = User.builder()
                    .firstName("John")
                    .lastName("Doe")
                    .email("student@lms.com")
                    .password(passwordEncoder.encode("student123"))
                    .role(Role.STUDENT)
                    .build();
            userRepository.save(student);
        }

        // Seed data for books
        if (authorRepository.count() == 0) {
            Author author = authorRepository.save(Author.builder().name("J.K. Rowling").bio("Author of Harry Potter").build());
            Category category = categoryRepository.save(Category.builder().name("Fantasy").description("Magical worlds and adventures").build());
            
            bookRepository.save(Book.builder()
                    .title("Harry Potter and the Sorcerer's Stone")
                    .isbn("9780590353427")
                    .totalCopies(10)
                    .availableCopies(10)
                    .author(author)
                    .category(category)
                    .build());
        }
    }
}
