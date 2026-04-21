package com.lms.services;

import com.lms.dtos.BookRequestDTO;
import com.lms.dtos.BookResponseDTO;
import com.lms.entities.Author;
import com.lms.entities.Book;
import com.lms.entities.Category;
import com.lms.exceptions.ResourceNotFoundException;
import com.lms.repositories.AuthorRepository;
import com.lms.repositories.BookRepository;
import com.lms.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookService bookService;

    private Author author;
    private Category category;
    private Book book;
    private BookRequestDTO bookRequestDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        author = new Author("J.K. Rowling", "Author of Harry Potter");
        author.setId(1L);

        category = new Category("Fantasy", "Magical worlds");
        category.setId(1L);

        book = new Book("Harry Potter", "1234567890", 10, 10, author, category);
        book.setId(1L);

        bookRequestDTO = new BookRequestDTO();
        bookRequestDTO.setTitle("Harry Potter");
        bookRequestDTO.setIsbn("1234567890");
        bookRequestDTO.setTotalCopies(10);
        bookRequestDTO.setAuthorId(1L);
        bookRequestDTO.setCategoryId(1L);
    }

    @Test
    void createBook_Success() {
        when(authorRepository.findById(1L)).thenReturn(Optional.of(author));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        BookResponseDTO response = bookService.createBook(bookRequestDTO);

        assertNotNull(response);
        assertEquals(book.getTitle(), response.getTitle());
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void createBook_AuthorNotFound() {
        when(authorRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.createBook(bookRequestDTO));
    }

    @Test
    void getBookById_Success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        BookResponseDTO response = bookService.getBookById(1L);

        assertNotNull(response);
        assertEquals(book.getId(), response.getId());
    }

    @Test
    void getBookById_NotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookService.getBookById(1L));
    }

    @Test
    void deleteBook_Success() {
        when(bookRepository.existsById(1L)).thenReturn(true);

        bookService.deleteBook(1L);

        verify(bookRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteBook_NotFound() {
        when(bookRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> bookService.deleteBook(1L));
    }
}
