package com.lms.dtos;

import com.lms.entities.LoanStatus;
import java.time.LocalDateTime;

public class LoanResponseDTO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private Long userId;
    private String userEmail;
    private LocalDateTime borrowDate;
    private LocalDateTime dueDate;
    private LocalDateTime returnDate;
    private LoanStatus status;

    public LoanResponseDTO() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookId() { return bookId; }
    public void setBookId(Long bookId) { this.bookId = bookId; }
    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public LocalDateTime getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDateTime borrowDate) { this.borrowDate = borrowDate; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public LocalDateTime getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDateTime returnDate) { this.returnDate = returnDate; }
    public LoanStatus getStatus() { return status; }
    public void setStatus(LoanStatus status) { this.status = status; }

    private long daysOverdue;
    private long fineAccrued;
    private long fineOutstanding;
    private boolean finePaid;

    public long getDaysOverdue() { return daysOverdue; }
    public void setDaysOverdue(long daysOverdue) { this.daysOverdue = daysOverdue; }
    public long getFineAccrued() { return fineAccrued; }
    public void setFineAccrued(long fineAccrued) { this.fineAccrued = fineAccrued; }
    public long getFineOutstanding() { return fineOutstanding; }
    public void setFineOutstanding(long fineOutstanding) { this.fineOutstanding = fineOutstanding; }
    public boolean isFinePaid() { return finePaid; }
    public void setFinePaid(boolean finePaid) { this.finePaid = finePaid; }
}
