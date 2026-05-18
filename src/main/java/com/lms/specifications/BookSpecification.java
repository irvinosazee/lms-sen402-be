package com.lms.specifications;

import com.lms.entities.Book;
import org.springframework.data.jpa.domain.Specification;

/** Builds the WHERE clause for book search. JPA Criteria API — case-insensitive substring match. */
public class BookSpecification {

    /** Returns a Specification matching title OR author.name OR category.name OR isbn. */
    public static Specification<Book> search(String query) {
        return (root, cq, cb) -> {
            // No filter: WHERE 1=1 (matches everything).
            if (query == null || query.isEmpty()) {
                return cb.conjunction();
            }
            String likePattern = "%" + query.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), likePattern),
                    cb.like(cb.lower(root.join("author").get("name")), likePattern),
                    cb.like(cb.lower(root.join("category").get("name")), likePattern),
                    cb.like(cb.lower(root.get("isbn")), likePattern)
            );
        };
    }
}
