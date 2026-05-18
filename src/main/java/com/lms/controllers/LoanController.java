package com.lms.controllers;

import com.lms.dtos.LoanRequestDTO;
import com.lms.dtos.LoanResponseDTO;
import com.lms.services.LoanService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Borrow / return / settle fines + listings. Ownership check on return lives in the service. */
@RestController
@RequestMapping("/api/v1/loans")
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    /** Students only — staff don't borrow books for themselves through this app. */
    @PostMapping("/borrow")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<LoanResponseDTO> borrowBook(@Valid @RequestBody LoanRequestDTO request) {
        return ResponseEntity.ok(loanService.borrowBook(request.getBookId()));
    }

    /** Any authenticated user; the service checks "owner or staff" and rejects others with 403. */
    @PostMapping("/{loanId}/return")
    public ResponseEntity<LoanResponseDTO> returnBook(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.returnBook(loanId));
    }

    /** Librarian/admin only — students pay at the desk on return, they don't self-settle. */
    @PostMapping("/{loanId}/settle-fine")
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<LoanResponseDTO> settleFine(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanService.settleFine(loanId));
    }

    /** Student's own loans. */
    @GetMapping("/my")
    public ResponseEntity<List<LoanResponseDTO>> getMyLoans() {
        return ResponseEntity.ok(loanService.getMyLoans());
    }

    /** All loans system-wide (staff). */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LIBRARIAN')")
    public ResponseEntity<List<LoanResponseDTO>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }
}
