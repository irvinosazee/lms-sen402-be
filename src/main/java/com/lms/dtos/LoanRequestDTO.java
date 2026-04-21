package com.lms.dtos;

import jakarta.validation.constraints.NotNull;

public class LoanRequestDTO {
    @NotNull(message = "Book ID is required")
    private Long bookId;
    public LoanRequestDTO() {}
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
}
