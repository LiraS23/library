package com.liras23.library.book;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    /**
     * Finds all books whose title contains the given string, ignoring case.
     *
     * @param title    The string to search for in the book's title.
     * @param pageable The pagination information.
     * @return A page of books matching the criteria.
     */
    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    /**
     * Finds a book by its ISBN, ignoring case.
     *
     * @param isbn The ISBN to search for.
     * @return An Optional containing the book if found.
     */
    Optional<Book> findByIsbnIgnoreCase(String isbn);
}
