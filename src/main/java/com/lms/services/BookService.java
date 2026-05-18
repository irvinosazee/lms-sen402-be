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

/** CRUD for books. Search/pagination is delegated to BookSpecification. */
@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final CategoryRepository categoryRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository,
                       CategoryRepository categoryRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.categoryRepository = categoryRepository;
    }

    /** New books start with available == total (everything on the shelf). */
    public BookResponseDTO createBook(BookRequestDTO request) {
        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Book book = new Book(request.getTitle(), request.getIsbn(),
                             request.getTotalCopies(), request.getTotalCopies(),
                             author, category);
        return mapToResponse(bookRepository.save(book));
    }

    /** Paginated + searchable list. {@code query} matches title/author/category/isbn. */
    public Page<BookResponseDTO> getAllBooks(String query, Pageable pageable) {
        Specification<Book> spec = BookSpecification.search(query);
        return bookRepository.findAll(spec, pageable).map(this::mapToResponse);
    }

    public BookResponseDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        return mapToResponse(book);
    }

    /** Keeps checked-out books checked out when totalCopies changes (delta applied to available). */
    @Transactional
    public BookResponseDTO updateBook(Long id, BookRequestDTO request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        // E.g., total goes 10 → 12 (bought 2 more) ⇒ available also goes up by 2.
        int diff = request.getTotalCopies() - book.getTotalCopies();
        book.setTitle(request.getTitle());
        book.setIsbn(request.getIsbn());
        book.setTotalCopies(request.getTotalCopies());
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

    /** Entity → DTO. Nested author/category use the 3-arg constructor so bookCount stays null. */
    private BookResponseDTO mapToResponse(Book book) {
        BookResponseDTO response = new BookResponseDTO();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setIsbn(book.getIsbn());
        response.setTotalCopies(book.getTotalCopies());
        response.setAvailableCopies(book.getAvailableCopies());

        // bookCount left null on purpose — @JsonInclude(NON_NULL) strips it from the wire.
        response.setAuthor(new AuthorResponseDTO(
            book.getAuthor().getId(), book.getAuthor().getName(), book.getAuthor().getBio()));
        response.setCategory(new CategoryResponseDTO(
            book.getCategory().getId(), book.getCategory().getName(), book.getCategory().getDescription()));
        return response;
    }
}
