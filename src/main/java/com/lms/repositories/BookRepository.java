package com.lms.repositories;

import com.lms.entities.Author;
import com.lms.entities.Book;
import com.lms.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    long countByCategory(Category category);
    long countByAuthor(Author author);
}
