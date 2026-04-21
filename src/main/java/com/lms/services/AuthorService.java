package com.lms.services;

import com.lms.dtos.AuthorResponseDTO;
import com.lms.entities.Author;
import com.lms.repositories.AuthorRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;
    
    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }
    
    public AuthorResponseDTO createAuthor(String name, String bio) {
        Author author = new Author(name, bio);
        author = authorRepository.save(author);
        return new AuthorResponseDTO(author.getId(), author.getName(), author.getBio());
    }
    public List<AuthorResponseDTO> getAllAuthors() {
        return authorRepository.findAll().stream()
            .map(a -> new AuthorResponseDTO(a.getId(), a.getName(), a.getBio()))
            .collect(Collectors.toList());
    }
}
