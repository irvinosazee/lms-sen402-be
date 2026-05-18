package com.lms.entities;

import jakarta.persistence.*;
import java.util.List;

/** Book author. Many books per author (OneToMany), but the FK lives on Book. */
@Entity
@Table(name = "authors")
public class Author {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String bio;

    // mappedBy = "author" means Book owns the FK column; this side is read-only.
    @OneToMany(mappedBy = "author")
    private List<Book> books;

    public Author() {}

    public Author(String name, String bio) {
        this.name = name;
        this.bio = bio;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public List<Book> getBooks() { return books; }
    public void setBooks(List<Book> books) { this.books = books; }
}
