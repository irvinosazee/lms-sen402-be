package com.lms.repositories;

import com.lms.entities.Author;
import org.springframework.data.jpa.repository.JpaRepository;

/** Inherits save/findById/findAll/delete from JpaRepository. No custom queries needed. */
public interface AuthorRepository extends JpaRepository<Author, Long> {
}
