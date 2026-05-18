package com.lms.controllers;

import com.lms.dtos.AuthorRequestDTO;
import com.lms.dtos.AuthorResponseDTO;
import com.lms.services.AuthorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Author CRUD. Same shape as CategoryController. */
@RestController
@RequestMapping("/api/v1/authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping
    public ResponseEntity<List<AuthorResponseDTO>> list() {
        return ResponseEntity.ok(authorService.getAll());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<AuthorResponseDTO> create(@Valid @RequestBody AuthorRequestDTO request) {
        return ResponseEntity.ok(authorService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<AuthorResponseDTO> update(@PathVariable Long id,
                                                    @Valid @RequestBody AuthorRequestDTO request) {
        return ResponseEntity.ok(authorService.update(id, request));
    }

    /** Returns 409 if books still reference the author (service-layer guard). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
