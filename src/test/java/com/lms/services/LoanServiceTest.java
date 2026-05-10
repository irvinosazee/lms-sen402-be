package com.lms.services;

import com.lms.dtos.LoanResponseDTO;
import com.lms.entities.*;
import com.lms.exceptions.BadRequestException;
import com.lms.exceptions.ResourceNotFoundException;
import com.lms.repositories.BookRepository;
import com.lms.repositories.LoanRepository;
import com.lms.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LoanService loanService;

    private User user;
    private Book book;
    private Loan loan;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        user = new User("student@lms.com", "password", "John", "Doe", Role.STUDENT);
        user.setId(1L);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                Collections.emptyList()
        );
        SecurityContext securityContext = new SecurityContextImpl();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        Author author = new Author("Author", "Bio");
        Category category = new Category("Category", "Desc");
        book = new Book("Title", "ISBN", 5, 5, author, category);
        book.setId(1L);

        loan = new Loan(book, user, LocalDateTime.now(), LocalDateTime.now().plusDays(14), LoanStatus.BORROWED);
        loan.setId(1L);
    }

    @Test
    void borrowBook_Success() {
        when(userRepository.findByEmail("student@lms.com")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        LoanResponseDTO response = loanService.borrowBook(1L);

        assertNotNull(response);
        assertEquals(4, book.getAvailableCopies());
        verify(bookRepository, times(1)).save(book);
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void borrowBook_NoCopiesAvailable() {
        book.setAvailableCopies(0);
        when(userRepository.findByEmail("student@lms.com")).thenReturn(Optional.of(user));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> loanService.borrowBook(1L));
        assertEquals("No copies available for borrowing", exception.getMessage());
    }

    @Test
    void returnBook_Success() {
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        LoanResponseDTO response = loanService.returnBook(1L);

        assertNotNull(response);
        assertEquals(LoanStatus.RETURNED, response.getStatus());
        assertEquals(6, book.getAvailableCopies());
        assertNotNull(loan.getReturnDate());
        verify(bookRepository, times(1)).save(book);
    }

    @Test
    void returnBook_AlreadyReturned() {
        loan.setStatus(LoanStatus.RETURNED);
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        BadRequestException exception = assertThrows(BadRequestException.class, () -> loanService.returnBook(1L));
        assertEquals("Book already returned", exception.getMessage());
    }
}
