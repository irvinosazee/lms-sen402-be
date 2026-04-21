package com.lms.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BookResponseDTO {
    private Long id;
    private String title;
    private String isbn;
    private Integer totalCopies;
    private Integer availableCopies;
    private AuthorResponseDTO author;
    private CategoryResponseDTO category;
}
