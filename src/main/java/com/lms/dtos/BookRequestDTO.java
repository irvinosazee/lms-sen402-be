package com.lms.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
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
}
