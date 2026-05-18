package com.lms.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Author on the wire. bookCount populated by list endpoints; null + stripped when embedded in a book. */
public class AuthorResponseDTO {
    private Long id;
    private String name;
    private String bio;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long bookCount;

    public AuthorResponseDTO() {}

    /** 3-arg ctor — bookCount stays null (used when embedded inside BookResponseDTO). */
    public AuthorResponseDTO(Long id, String name, String bio) {
        this.id = id;
        this.name = name;
        this.bio = bio;
    }

    /** 4-arg ctor — populates bookCount for list endpoints. */
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
