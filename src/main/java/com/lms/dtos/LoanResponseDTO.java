package com.lms.dtos;

import com.lms.entities.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
}
