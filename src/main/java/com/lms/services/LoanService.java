package com.lms.services;

import com.lms.dtos.LoanResponseDTO;
import com.lms.entities.*;
import com.lms.exceptions.ResourceNotFoundException;
import com.lms.repositories.BookRepository;
import com.lms.repositories.LoanRepository;
import com.lms.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Transactional
    public LoanResponseDTO borrowBook(Long bookId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No copies available for borrowing");
        }

        // Create loan
        Loan loan = Loan.builder()
                .book(book)
                .user(user)
                .borrowDate(LocalDateTime.now())
                .dueDate(LocalDateTime.now().plusDays(14)) // Default 2 weeks
                .status(LoanStatus.BORROWED)
                .build();

        // Update book availability
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
        
        Loan savedLoan = loanRepository.save(loan);
        return mapToResponse(savedLoan);
    }

    @Transactional
    public LoanResponseDTO returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new RuntimeException("Book already returned");
        }

        loan.setReturnDate(LocalDateTime.now());
        loan.setStatus(LoanStatus.RETURNED);

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        bookRepository.save(book);

        Loan savedLoan = loanRepository.save(loan);
        return mapToResponse(savedLoan);
    }

    public List<LoanResponseDTO> getMyLoans() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return loanRepository.findByUser(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<LoanResponseDTO> getAllLoans() {
        return loanRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private LoanResponseDTO mapToResponse(Loan loan) {
        return LoanResponseDTO.builder()
                .id(loan.getId())
                .bookId(loan.getBook().getId())
                .bookTitle(loan.getBook().getTitle())
                .userId(loan.getUser().getId())
                .userEmail(loan.getUser().getEmail())
                .borrowDate(loan.getBorrowDate())
                .dueDate(loan.getDueDate())
                .returnDate(loan.getReturnDate())
                .status(loan.getStatus())
                .build();
    }
}
