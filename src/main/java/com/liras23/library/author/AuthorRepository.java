package com.liras23.library.author;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuthorRepository extends JpaRepository<Author, UUID> {

    /**
     * Finds all authors whose name contains the given string, ignoring case.
     *
     * @param name     The string to search for in the author's name.
     * @param pageable The pagination information.
     * @return A page of authors matching the criteria.
     */
    Page<Author> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
