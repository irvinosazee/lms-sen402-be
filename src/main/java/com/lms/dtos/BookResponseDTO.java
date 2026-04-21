package com.lms.dtos;

public class BookResponseDTO {
    private Long id;
    private String title;
    private String isbn;
    private Integer totalCopies;
    private Integer availableCopies;
    private AuthorResponseDTO author;
    private CategoryResponseDTO category;

    public BookResponseDTO() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public Integer getTotalCopies() { return totalCopies; }
    public void setTotalCopies(Integer totalCopies) { this.totalCopies = totalCopies; }
    public Integer getAvailableCopies() { return availableCopies; }
    public void setAvailableCopies(Integer availableCopies) { this.availableCopies = availableCopies; }
    public AuthorResponseDTO getAuthor() { return author; }
    public void setAuthor(AuthorResponseDTO author) { this.author = author; }
    public CategoryResponseDTO getCategory() { return category; }
    public void setCategory(CategoryResponseDTO category) { this.category = category; }
}
