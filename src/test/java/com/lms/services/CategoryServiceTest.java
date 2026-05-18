package com.lms.services;

import com.lms.dtos.CategoryRequestDTO;
import com.lms.entities.Category;
import com.lms.exceptions.BadRequestException;
import com.lms.exceptions.ConflictException;
import com.lms.repositories.BookRepository;
import com.lms.repositories.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private BookRepository bookRepository;

    private CategoryService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CategoryService(categoryRepository, bookRepository);
    }

    @Test
    void create_rejectsDuplicateName() {
        CategoryRequestDTO request = new CategoryRequestDTO();
        request.setName("Fantasy");
        request.setDescription("Magical worlds");
        Category existing = new Category("Fantasy", "old description");
        existing.setId(1L);

        when(categoryRepository.findByName("Fantasy")).thenReturn(Optional.of(existing));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> service.create(request));
        assertEquals("A category named \"Fantasy\" already exists", ex.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void delete_blockedWhenBooksReference() {
        Category category = new Category("Fantasy", "");
        category.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(bookRepository.countByCategory(category)).thenReturn(4L);

        ConflictException ex = assertThrows(ConflictException.class, () -> service.delete(1L));
        assertEquals(
                "Cannot delete category \"Fantasy\" — 4 books are assigned to it. "
                        + "Reassign or delete those books first.",
                ex.getMessage());
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void delete_succeedsWhenNoBooks() {
        Category category = new Category("Fantasy", "");
        category.setId(1L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(bookRepository.countByCategory(category)).thenReturn(0L);

        service.delete(1L);

        verify(categoryRepository).deleteById(1L);
    }
}
