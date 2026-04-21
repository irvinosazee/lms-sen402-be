package com.lms.services;

import com.lms.dtos.*;
import com.lms.entities.Book;
import com.lms.entities.Author;
import com.lms.entities.Category;
import com.lms.exceptions.ResourceNotFoundException;
import com.lms.repositories.BookRepository;
import com.lms.repositories.AuthorRepository;
import com.lms.repositories.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    public BookResponseDTO createBook(BookRequestDTO request) {
        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Book book = Book.builder()
                .title(request.getTitle())
                .isbn(request.getIsbn())
                .totalCopies(request.getTotalCopies())
                .availableCopies(request.getTotalCopies())
                .author(author)
                .category(category)
                .build();

        Book savedBook = bookRepository.save(book);
        return mapToResponse(savedBook);
    }

    public List<BookResponseDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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

        // Update copies logic
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
        return BookResponseDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .isbn(book.getIsbn())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .author(AuthorResponseDTO.builder()
                        .id(book.getAuthor().getId())
                        .name(book.getAuthor().getName())
                        .bio(book.getAuthor().getBio())
                        .build())
                .category(CategoryResponseDTO.builder()
                        .id(book.getCategory().getId())
                        .name(book.getCategory().getName())
                        .description(book.getCategory().getDescription())
                        .build())
                .build();
    }
}
