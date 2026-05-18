package com.lms.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

public class AuthorResponseDTO {
    private Long id;
    private String name;
    private String bio;

    // Number of books currently assigned to this author. Same convention as
    // CategoryResponseDTO.bookCount — populated by list endpoints, omitted
    // from JSON when embedded inside a BookResponseDTO.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long bookCount;

    public AuthorResponseDTO() {}

    public AuthorResponseDTO(Long id, String name, String bio) {
        this.id = id;
        this.name = name;
        this.bio = bio;
    }

    public AuthorResponseDTO(Long id, String name, String bio, Long bookCount) {
        this.id = id;
        this.name = name;
        this.bio = bio;
        this.bookCount = bookCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public Long getBookCount() { return bookCount; }
    public void setBookCount(Long bookCount) { this.bookCount = bookCount; }
}
