package com.lms.controllers;

import com.lms.dtos.AuthorResponseDTO;
import com.lms.services.AuthorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
public class AuthorController {
    private final AuthorService authorService;
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<AuthorResponseDTO> createAuthor(@RequestBody AuthorResponseDTO request) {
        return ResponseEntity.ok(authorService.createAuthor(request.getName(), request.getBio()));
    }
    @GetMapping
    public ResponseEntity<List<AuthorResponseDTO>> getAllAuthors() {
        return ResponseEntity.ok(authorService.getAllAuthors());
    }
}
