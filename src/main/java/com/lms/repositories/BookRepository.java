package com.lms.repositories;

import com.lms.entities.Author;
import com.lms.entities.Book;
import com.lms.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Books. JpaSpecificationExecutor adds findAll(Specification, Pageable) — used by BookService
 * to support dynamic search via BookSpecification.
 */
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    /** Used as the delete-guard in CategoryService.delete. */
    long countByCategory(Category category);

    /** Used as the delete-guard in AuthorService.delete. */
    long countByAuthor(Author author);
}
