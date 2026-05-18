package com.lms.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;

public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String description;

    // Number of books currently assigned to this category. Populated by list
    // endpoints. Left null (and omitted from JSON) when the DTO is embedded
    // inside a BookResponseDTO, to avoid an N+1 query per book.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Long bookCount;

    public CategoryResponseDTO() {}

    public CategoryResponseDTO(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

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
