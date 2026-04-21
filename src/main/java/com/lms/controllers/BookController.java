package com.lms.controllers;

import com.lms.dtos.BookRequestDTO;
import com.lms.dtos.BookResponseDTO;
import com.lms.services.BookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody BookRequestDTO request) {
        return new ResponseEntity<>(bookService.createBook(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BookResponseDTO>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> getBookById(@PathVariable Long id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BookResponseDTO> updateBook(@PathVariable Long id, @Valid @RequestBody BookRequestDTO request) {
        return ResponseEntity.ok(bookService.updateBook(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }
}
