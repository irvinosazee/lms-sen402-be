package com.lms.controllers;

import com.lms.dtos.LoanRequestDTO;
import com.lms.dtos.LoanResponseDTO;
import com.lms.services.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/{loanId}/return")
    public ResponseEntity<LoanResponseDTO> returnBook(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.returnBook(loanId));
    }

    @GetMapping("/my")
    public ResponseEntity<List<LoanResponseDTO>> getMyLoans() {
        return ResponseEntity.ok(loanService.getMyLoans());
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<LoanResponseDTO>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }
}
