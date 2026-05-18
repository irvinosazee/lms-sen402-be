package com.lms.repositories;

import com.lms.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Used by CategoryService to pre-check the unique-name constraint. */
    Optional<Category> findByName(String name);
}
