package com.lms.dtos;

import java.time.LocalDateTime;

public class DashboardActivityDTO {
    private String type; // BORROW or RETURN
    private String bookTitle;
    private String userEmail;
    private LocalDateTime timestamp;

    public DashboardActivityDTO(String type, String bookTitle, String userEmail, LocalDateTime timestamp) {
        this.type = type;
        this.bookTitle = bookTitle;
        this.userEmail = userEmail;
        this.timestamp = timestamp;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
