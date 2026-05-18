package com.lms.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AuthorRequestDTO {

    @NotBlank(message = "Author name is required")
    @Size(max = 200, message = "Author name must be 200 characters or fewer")
    private String name;

    @Size(max = 5000, message = "Bio must be 5000 characters or fewer")
    private String bio;

    public AuthorRequestDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
