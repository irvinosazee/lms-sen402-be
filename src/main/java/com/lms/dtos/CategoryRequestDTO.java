package com.lms.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Create/update category payload. Name uniqueness is enforced in the service. */
public class CategoryRequestDTO {

    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must be 100 characters or fewer")
    private String name;

    @Size(max = 2000, message = "Description must be 2000 characters or fewer")
    private String description;

    public CategoryRequestDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
