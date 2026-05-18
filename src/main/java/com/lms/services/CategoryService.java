package com.lms.services;

import com.lms.dtos.CategoryRequestDTO;
import com.lms.dtos.CategoryResponseDTO;
import com.lms.entities.Category;
import com.lms.exceptions.BadRequestException;
import com.lms.exceptions.ConflictException;
import com.lms.exceptions.ResourceNotFoundException;
import com.lms.repositories.BookRepository;
import com.lms.repositories.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** CRUD for categories. Unique names; deletion blocked while books reference them. */
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final BookRepository bookRepository;

    public CategoryService(CategoryRepository categoryRepository, BookRepository bookRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
    }

    public List<CategoryResponseDTO> getAll() {
        return categoryRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public CategoryResponseDTO create(CategoryRequestDTO request) {
        String name = request.getName().trim();
        // Pre-check uniqueness to surface a clean 400 instead of a raw DB constraint error.
        if (categoryRepository.findByName(name).isPresent()) {
            throw new BadRequestException("A category named \"" + name + "\" already exists");
        }
        return toResponse(categoryRepository.save(new Category(name, request.getDescription())));
    }

    @Transactional
    public CategoryResponseDTO update(Long id, CategoryRequestDTO request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        String name = request.getName().trim();
        // OK if the matching row IS this one (user saved without changing the name).
        Optional<Category> clash = categoryRepository.findByName(name);
        if (clash.isPresent() && !clash.get().getId().equals(id)) {
            throw new BadRequestException("A category named \"" + name + "\" already exists");
        }

        category.setName(name);
        category.setDescription(request.getDescription());
        return toResponse(categoryRepository.save(category));
    }

    /** Refuses to delete if any books reference the category. Returns 409 with the count. */
    @Transactional
    public void delete(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        long bookCount = bookRepository.countByCategory(category);
        if (bookCount > 0) {
            String plural = bookCount == 1 ? "book is" : "books are";
            throw new ConflictException(
                    "Cannot delete category \"" + category.getName() + "\" — "
                            + bookCount + " " + plural
                            + " assigned to it. Reassign or delete those books first.");
        }
        categoryRepository.deleteById(id);
    }

    /** 4-arg DTO ctor — populates bookCount. BookService uses the 3-arg ctor instead (avoids N+1). */
    private CategoryResponseDTO toResponse(Category category) {
        return new CategoryResponseDTO(category.getId(), category.getName(),
                category.getDescription(), bookRepository.countByCategory(category));
    }
}
