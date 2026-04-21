package com.lms.specifications;

import com.lms.entities.Book;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecification {

    public static Specification<Book> search(String query) {
        return (root, cq, cb) -> {
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
