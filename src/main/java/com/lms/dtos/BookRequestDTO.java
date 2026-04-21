package com.lms.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class BookRequestDTO {
    @NotBlank(message = "Title is required")
    private String title;
    @NotBlank(message = "ISBN is required")
    private String isbn;
    @NotNull(message = "Total copies is required")
    @PositiveOrZero(message = "Total copies must be zero or positive")
    private Integer totalCopies;
    @NotNull(message = "Author ID is required")
    private Long authorId;
    @NotNull(message = "Category ID is required")
    private Long categoryId;

    public BookRequestDTO() {}
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getTotalCopies() { return totalCopies; }
    public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
}
