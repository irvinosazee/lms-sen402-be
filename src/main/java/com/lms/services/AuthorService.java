package com.lms.services;

import com.lms.dtos.AuthorResponseDTO;
import com.lms.entities.Author;
import com.lms.repositories.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorService {
    private final AuthorRepository authorRepository;
    public AuthorResponseDTO createAuthor(String name, String bio) {
        Author author = Author.builder().name(name).bio(bio).build();
        author = authorRepository.save(author);
        return AuthorResponseDTO.builder().id(author.getId()).name(author.getName()).bio(author.getBio()).build();
    }
    public List<AuthorResponseDTO> getAllAuthors() {
        return authorRepository.findAll().stream()
            .map(a -> AuthorResponseDTO.builder().id(a.getId()).name(a.getName()).bio(a.getBio()).build())
            .collect(Collectors.toList());
    }
}
