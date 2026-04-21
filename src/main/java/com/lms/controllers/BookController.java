package com.lms.controllers;

import com.lms.dtos.BookRequestDTO;
import com.lms.dtos.BookResponseDTO;
import com.lms.services.BookService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<BookResponseDTO> createBook(@Valid @RequestBody BookRequestDTO request) {
        return new ResponseEntity<>(bookService.createBook(request), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<Page<BookResponseDTO>> getAllBooks(
            @RequestParam(required = false) String query,
            Pageable pageable) {
        return ResponseEntity.ok(bookService.getAllBooks(query, pageable));
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
