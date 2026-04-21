package com.lms.services;

import com.lms.dtos.*;
import com.lms.entities.Book;
import com.lms.entities.Author;
import com.lms.entities.Category;
import com.lms.exceptions.ResourceNotFoundException;
import com.lms.repositories.BookRepository;
import com.lms.repositories.AuthorRepository;
import com.lms.repositories.CategoryRepository;
import com.lms.specifications.BookSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
    }

    public BookResponseDTO createBook(BookRequestDTO request) {
        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Book book = new Book(
            request.getTitle(),
            request.getIsbn(),
            request.getTotalCopies(),
            request.getTotalCopies(),
            author,
            category
        );

        Book savedBook = bookRepository.save(book);
        return mapToResponse(savedBook);
    }

    public Page<BookResponseDTO> getAllBooks(String query, Pageable pageable) {
        Specification<Book> spec = BookSpecification.search(query);
        return bookRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    public BookResponseDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        return mapToResponse(book);
    }

    @Transactional
    public BookResponseDTO updateBook(Long id, BookRequestDTO request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        int oldTotal = book.getTotalCopies();
        int newTotal = request.getTotalCopies();
        int diff = newTotal - oldTotal;
        
        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setTotalCopies(newTotal);
        book.setAvailableCopies(book.getAvailableCopies() + diff);
        book.setAuthor(author);
        book.setCategory(category);

        return mapToResponse(bookRepository.save(book));
    }

    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found");
        }
        bookRepository.deleteById(id);
    }

    private BookResponseDTO mapToResponse(Book book) {
        BookResponseDTO response = new BookResponseDTO();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setIsbn(book.getIsbn());
        response.setTotalCopies(book.getTotalCopies());
        response.setAvailableCopies(book.getAvailableCopies());
        
        AuthorResponseDTO authorDTO = new AuthorResponseDTO(
            book.getAuthor().getId(),
            book.getAuthor().getName(),
            book.getAuthor().getBio()
        );
        response.setAuthor(authorDTO);

        CategoryResponseDTO categoryDTO = new CategoryResponseDTO(
            book.getCategory().getId(),
            book.getCategory().getName(),
            book.getCategory().getDescription()
        );
        response.setCategory(categoryDTO);
        
        return response;
    }
}
