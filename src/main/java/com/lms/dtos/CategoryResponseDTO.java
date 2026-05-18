package com.lms.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Category on the wire. Same bookCount convention as AuthorResponseDTO. */
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String description;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long bookCount;

    public CategoryResponseDTO() {}

    /** 3-arg ctor — bookCount stays null (used when embedded inside BookResponseDTO). */
    public CategoryResponseDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    /** 4-arg ctor — populates bookCount for list endpoints. */
    public CategoryResponseDTO(Long id, String name, String description, Long bookCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.bookCount = bookCount;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getBookCount() { return bookCount; }
    public void setBookCount(Long bookCount) { this.bookCount = bookCount; }
}
