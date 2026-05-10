package com.lms.services;

import com.lms.dtos.LoanResponseDTO;
import com.lms.entities.*;
import com.lms.exceptions.BadRequestException;
import com.lms.exceptions.ForbiddenException;
import com.lms.exceptions.ResourceNotFoundException;
import com.lms.repositories.BookRepository;
import com.lms.repositories.LoanRepository;
import com.lms.repositories.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LoanService {

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    private final long finePerDay;

    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       UserRepository userRepository,
                       @org.springframework.beans.factory.annotation.Value("${app.loan.fine-per-day-naira:100}") long finePerDay) {
        if (finePerDay < 0) {
            throw new IllegalStateException("app.loan.fine-per-day-naira must be >= 0");
        }
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.finePerDay = finePerDay;
    }

    @Transactional
    public LoanResponseDTO borrowBook(Long bookId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        var unpaid = loanRepository.findByUserAndFinePaidFalse(user);
        boolean hasOutstandingFine = unpaid.stream()
                .anyMatch(l -> LoanFines.fineAccrued(l, finePerDay) > 0);
        if (hasOutstandingFine) {
            throw new BadRequestException("Settle outstanding fines before borrowing again");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new BadRequestException("No copies available for borrowing");
        }

        Loan loan = new Loan(
            book,
            user,
            LocalDateTime.now(),
            LocalDateTime.now().plusDays(14),
            LoanStatus.BORROWED
        );

        book.setAvailableCopies(book.getAvailableCopies() - 1);
        bookRepository.save(book);
        
        Loan savedLoan = loanRepository.save(loan);
        return mapToResponse(savedLoan);
    }

    @Transactional
    public LoanResponseDTO returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        var auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        boolean isStaff = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_LIBRARIAN"));
        if (!isStaff && !loan.getUser().getEmail().equals(email)) {
            throw new ForbiddenException("You can only return your own loans");
        }

        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BadRequestException("Book already returned");
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

    @Transactional
    public LoanResponseDTO settleFine(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (loan.getStatus() == LoanStatus.BORROWED) {
            throw new BadRequestException("Fine can only be settled after the book is returned");
        }
        if (loan.isFinePaid()) {
            throw new BadRequestException("Fine already settled");
        }
        if (LoanFines.fineAccrued(loan, finePerDay) == 0) {
            throw new BadRequestException("No outstanding fine on this loan");
        }

        loan.setFinePaid(true);
        loan.setFinePaidAt(LocalDateTime.now());
        return mapToResponse(loanRepository.save(loan));
    }

    private LoanResponseDTO mapToResponse(Loan loan) {
        LoanResponseDTO response = new LoanResponseDTO();
        response.setId(loan.getId());
        response.setBookId(loan.getBook().getId());
        response.setBookTitle(loan.getBook().getTitle());
        response.setUserId(loan.getUser().getId());
        response.setUserEmail(loan.getUser().getEmail());
        response.setBorrowDate(loan.getBorrowDate());
        response.setDueDate(loan.getDueDate());
        response.setReturnDate(loan.getReturnDate());
        response.setStatus(loan.getStatus());

        long days = LoanFines.daysOverdue(loan);
        long accrued = days * finePerDay;
        response.setDaysOverdue(days);
        response.setFineAccrued(accrued);
        response.setFineOutstanding(loan.isFinePaid() ? 0L : accrued);
        response.setFinePaid(loan.isFinePaid());
        return response;
    }
}
