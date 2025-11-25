package com.liras23.library.book.service;

import com.liras23.library.author.Author;
import com.liras23.library.author.AuthorRepository;
import com.liras23.library.book.Book;
import com.liras23.library.book.BookRepository;
import com.liras23.library.book.dto.BookRequestDTO;
import com.liras23.library.book.dto.BookResponseDTO;
import com.liras23.library.book.mapper.BookMapper;
import com.liras23.library.common.exception.DuplicateResourceException;
import com.liras23.library.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookMapper bookMapper;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
        this.bookMapper = bookMapper;
    }

    @Transactional(readOnly = true)
    public Page<BookResponseDTO> findAll(String title, Pageable pageable) {
        Page<Book> page;
        if (StringUtils.hasText(title)) {
            page = bookRepository.findByTitleContainingIgnoreCase(title, pageable);
        } else {
            page = bookRepository.findAll(pageable);
        }
        return page.map(bookMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public BookResponseDTO findById(UUID id) {
        Book book = findBookById(id);
        return bookMapper.toResponse(book);
    }

    @Transactional
    public BookResponseDTO create(BookRequestDTO requestDTO) {
        validateIsbnUniqueness(requestDTO.isbn());
        Author author = findAuthorById(requestDTO.authorId());

        Book book = bookMapper.toEntity(requestDTO);
        book.setAuthor(author);
        // O ISBN já é setado pelo mapper, mas caso a lógica mude, garantimos aqui.
        book.setIsbn(requestDTO.isbn());

        Book savedBook = bookRepository.save(book);
        return bookMapper.toResponse(savedBook);
    }

    @Transactional
    public BookResponseDTO update(UUID id, BookRequestDTO requestDTO) {
        Book bookToUpdate = findBookById(id);
        validateIsbnUniqueness(requestDTO.isbn(), bookToUpdate.getId());
        Author author = findAuthorById(requestDTO.authorId());

        bookToUpdate.setTitle(requestDTO.title());
        bookToUpdate.setIsbn(requestDTO.isbn());
        bookToUpdate.setAuthor(author);

        Book updatedBook = bookRepository.save(bookToUpdate);
        return bookMapper.toResponse(updatedBook);
    }

    @Transactional
    public void delete(UUID id) {
        if (!bookRepository.existsById(id)) {
            throw new ResourceNotFoundException("Book not found with id: " + id);
        }
        bookRepository.deleteById(id);
    }

    private Book findBookById(UUID id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    private Author findAuthorById(UUID authorId) {
        return authorRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + authorId));
    }

    private void validateIsbnUniqueness(String isbn) {
        bookRepository.findByIsbnIgnoreCase(isbn)
                .ifPresent(book -> {
                    throw new DuplicateResourceException("A book with ISBN " + isbn + " already exists.");
                });
    }

    private void validateIsbnUniqueness(String isbn, UUID currentBookId) {
        Optional<Book> existingBook = bookRepository.findByIsbnIgnoreCase(isbn);
        existingBook.ifPresent(book -> {
            if (!book.getId().equals(currentBookId)) {
                throw new DuplicateResourceException("A book with ISBN " + isbn + " already exists.");
            }
        });
    }
}
