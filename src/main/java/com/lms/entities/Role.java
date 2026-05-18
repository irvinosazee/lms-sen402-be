package com.lms.entities;

/** User roles. Stored as the name (e.g. "ADMIN") via @Enumerated(STRING) on User.role. */
public enum Role {
    ADMIN,
    LIBRARIAN,
    STUDENT
}
