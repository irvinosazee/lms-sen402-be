package com.lms.services;

import com.lms.entities.Author;
import com.lms.exceptions.ConflictException;
import com.lms.repositories.AuthorRepository;
import com.lms.repositories.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthorServiceTest {

    @Mock private AuthorRepository authorRepository;
    @Mock private BookRepository bookRepository;

    private AuthorService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AuthorService(authorRepository, bookRepository);
    }

    @Test
    void delete_blockedWhenBooksReference() {
        Author author = new Author("J.K. Rowling", "");
        author.setId(1L);
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(bookRepository.countByAuthor(author)).thenReturn(1L);

        ConflictException ex = assertThrows(ConflictException.class, () -> service.delete(1L));
        assertEquals(
                "Cannot delete author \"J.K. Rowling\" — 1 book is assigned to them. "
                        + "Reassign or delete those books first.",
                ex.getMessage());
        verify(authorRepository, never()).deleteById(any());
    }

    @Test
    void delete_succeedsWhenNoBooks() {
        Author author = new Author("Unknown", "");
        author.setId(5L);
        when(authorRepository.findById(5L)).thenReturn(Optional.of(author));
        when(bookRepository.countByAuthor(author)).thenReturn(0L);

        service.delete(5L);

        verify(authorRepository).deleteById(5L);
    }
}
