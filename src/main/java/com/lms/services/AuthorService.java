package com.lms.services;

import com.lms.dtos.AuthorRequestDTO;
import com.lms.dtos.AuthorResponseDTO;
import com.lms.entities.Author;
import com.lms.exceptions.ConflictException;
import com.lms.exceptions.ResourceNotFoundException;
import com.lms.repositories.AuthorRepository;
import com.lms.repositories.BookRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final BookRepository bookRepository;

    public AuthorService(AuthorRepository authorRepository, BookRepository bookRepository) {
        this.authorRepository = authorRepository;
        this.bookRepository = bookRepository;
    }

    public List<AuthorResponseDTO> getAll() {
        return authorRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public AuthorResponseDTO create(AuthorRequestDTO request) {
        Author saved = authorRepository.save(new Author(request.getName().trim(), request.getBio()));
        return toResponse(saved);
    }

    @Transactional
    public AuthorResponseDTO update(Long id, AuthorRequestDTO request) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
        author.setName(request.getName().trim());
        author.setBio(request.getBio());
        return toResponse(authorRepository.save(author));
    }

    @Transactional
    public void delete(Long id) {
        Author author = authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found"));
        long bookCount = bookRepository.countByAuthor(author);
        if (bookCount > 0) {
            String plural = bookCount == 1 ? "book is" : "books are";
            throw new ConflictException(
                    "Cannot delete author \"" + author.getName() + "\" — "
                            + bookCount + " " + plural
                            + " assigned to them. Reassign or delete those books first.");
        }
        authorRepository.deleteById(id);
    }

    private AuthorResponseDTO toResponse(Author author) {
        long count = bookRepository.countByAuthor(author);
        return new AuthorResponseDTO(author.getId(), author.getName(), author.getBio(), count);
    }
}
