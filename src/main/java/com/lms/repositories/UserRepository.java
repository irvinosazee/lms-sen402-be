package com.lms.repositories;

import com.lms.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/** Spring Data implements this interface at runtime — we just declare the queries we need. */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Spring derives the SQL from the method name: WHERE email = ?. Used for login + JWT lookup. */
    Optional<User> findByEmail(String email);
}
