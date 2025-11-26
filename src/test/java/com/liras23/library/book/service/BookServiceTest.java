package com.liras23.library.book.service;

import com.liras23.library.author.Author;
import com.liras23.library.author.AuthorRepository;
import com.liras23.library.author.dto.AuthorResponseDTO;
import com.liras23.library.book.Book;
import com.liras23.library.book.BookRepository;
import com.liras23.library.book.dto.BookRequestDTO;
import com.liras23.library.book.dto.BookResponseDTO;
import com.liras23.library.book.mapper.BookMapper;
import com.liras23.library.common.exception.DuplicateResourceException;
import com.liras23.library.common.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookService bookService;

    @Test
    @DisplayName("FindById: Should throw ResourceNotFoundException when book is not found")
    void findById_whenBookNotFound_shouldThrowResourceNotFoundException() {
        // Given
        UUID bookId = UUID.randomUUID();
        when(bookRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> bookService.findById(bookId));
    }

    @Test
    @DisplayName("FindById: Should return book when book is found")
    void findById_whenBookFound_shouldReturnBook() {
        // Given
        Author author = new Author(UUID.randomUUID(), "J.R.R. Tolkien");
        Book book = new Book(UUID.randomUUID(), "The Hobbit", author, "978-0345339683");
        BookResponseDTO expectedResponse = new BookResponseDTO(book.getId(), book.getTitle(), new AuthorResponseDTO(author.getId(), author.getName()), book.getIsbn());

        when(bookRepository.findById(book.getId())).thenReturn(Optional.of(book));
        when(bookMapper.toResponse(book)).thenReturn(expectedResponse);

        // When
        BookResponseDTO actualResponse = bookService.findById(book.getId());

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @DisplayName("Create: Should create a new book successfully")
    void create_shouldCreateNewBook() {
        // Given
        Author author = new Author(UUID.randomUUID(), "George Orwell");
        BookRequestDTO requestDTO = new BookRequestDTO("1984", author.getId(), "978-0451524935");
        Book bookToSave = new Book(null, "1984", author, "978-0451524935");
        Book savedBook = new Book(UUID.randomUUID(), "1984", author, "978-0451524935");
        BookResponseDTO expectedResponse = new BookResponseDTO(savedBook.getId(), savedBook.getTitle(), new AuthorResponseDTO(author.getId(), author.getName()), savedBook.getIsbn());

        when(bookRepository.findByIsbnIgnoreCase(requestDTO.isbn())).thenReturn(Optional.empty());
        when(authorRepository.findById(requestDTO.authorId())).thenReturn(Optional.of(author));
        when(bookMapper.toEntity(requestDTO)).thenReturn(bookToSave);
        when(bookRepository.save(bookToSave)).thenReturn(savedBook);
        when(bookMapper.toResponse(savedBook)).thenReturn(expectedResponse);

        // When
        BookResponseDTO actualResponse = bookService.create(requestDTO);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @DisplayName("Create: Should throw DuplicateResourceException when ISBN already exists")
    void create_whenIsbnExists_shouldThrowDuplicateResourceException() {
        // Given
        BookRequestDTO requestDTO = new BookRequestDTO("1984", UUID.randomUUID(), "978-0451524935");
        when(bookRepository.findByIsbnIgnoreCase(requestDTO.isbn())).thenReturn(Optional.of(new Book()));

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> bookService.create(requestDTO));
        verify(authorRepository, never()).findById(any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Create: Should throw ResourceNotFoundException when author does not exist")
    void create_whenAuthorNotFound_shouldThrowResourceNotFoundException() {
        // Given
        BookRequestDTO requestDTO = new BookRequestDTO("1984", UUID.randomUUID(), "978-0451524935");
        when(bookRepository.findByIsbnIgnoreCase(requestDTO.isbn())).thenReturn(Optional.empty());
        when(authorRepository.findById(requestDTO.authorId())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> bookService.create(requestDTO));
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Update: Should update book successfully")
    void update_shouldUpdateBook() {
        // Given
        Author author = new Author(UUID.randomUUID(), "J.R.R. Tolkien");
        Book existingBook = new Book(UUID.randomUUID(), "The Hobbit", author, "978-0345339683");
        BookRequestDTO requestDTO = new BookRequestDTO("The Hobbit, or There and Back Again", author.getId(), "978-0345339683");
        Book updatedBook = new Book(existingBook.getId(), requestDTO.title(), author, requestDTO.isbn());
        BookResponseDTO expectedResponse = new BookResponseDTO(updatedBook.getId(), updatedBook.getTitle(), new AuthorResponseDTO(author.getId(), author.getName()), updatedBook.getIsbn());

        when(bookRepository.findById(existingBook.getId())).thenReturn(Optional.of(existingBook));
        when(bookRepository.findByIsbnIgnoreCase(requestDTO.isbn())).thenReturn(Optional.of(existingBook)); // ISBN belongs to the same book
        when(authorRepository.findById(requestDTO.authorId())).thenReturn(Optional.of(author));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBook);
        when(bookMapper.toResponse(updatedBook)).thenReturn(expectedResponse);

        // When
        BookResponseDTO actualResponse = bookService.update(existingBook.getId(), requestDTO);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    @DisplayName("Update: Should throw DuplicateResourceException when new ISBN belongs to another book")
    void update_whenIsbnExistsInAnotherBook_shouldThrowDuplicateResourceException() {
        // Given
        Book existingBook = new Book(UUID.randomUUID(), "The Hobbit", new Author(), "978-0345339683");
        Book anotherBookWithSameIsbn = new Book(UUID.randomUUID(), "Another Book", new Author(), "978-0000000001");
        BookRequestDTO requestDTO = new BookRequestDTO("The Hobbit", UUID.randomUUID(), anotherBookWithSameIsbn.getIsbn());

        when(bookRepository.findById(existingBook.getId())).thenReturn(Optional.of(existingBook));
        when(bookRepository.findByIsbnIgnoreCase(requestDTO.isbn())).thenReturn(Optional.of(anotherBookWithSameIsbn));

        // When & Then
        assertThrows(DuplicateResourceException.class, () -> bookService.update(existingBook.getId(), requestDTO));
        verify(bookRepository, never()).save(any());
    }

    @Test
    @DisplayName("Delete: Should delete book successfully when book exists")
    void delete_whenBookExists_shouldDeleteBook() {
        // Given
        UUID bookId = UUID.randomUUID();
        when(bookRepository.existsById(bookId)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(bookId);

        // When
        bookService.delete(bookId);

        // Then
        verify(bookRepository, times(1)).existsById(bookId);
        verify(bookRepository, times(1)).deleteById(bookId);
    }

    @Test
    @DisplayName("Delete: Should throw ResourceNotFoundException when book does not exist")
    void delete_whenBookDoesNotExist_shouldThrowResourceNotFoundException() {
        // Given
        UUID bookId = UUID.randomUUID();
        when(bookRepository.existsById(bookId)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> bookService.delete(bookId));
        verify(bookRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("FindAll: Should return a paginated list of books when no title is provided")
    void findAll_whenNoTitleProvided_shouldReturnAllBooks() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Book book = new Book(UUID.randomUUID(), "The Hobbit", new Author(), "978-0345339683");
        Page<Book> bookPage = new PageImpl<>(List.of(book), pageable, 1);

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(new BookResponseDTO(book.getId(), book.getTitle(), null, book.getIsbn()));

        // When
        Page<BookResponseDTO> actualResponse = bookService.findAll(null, pageable);

        // Then
        assertFalse(actualResponse.isEmpty());
        assertEquals(1, actualResponse.getTotalElements());
        verify(bookRepository, times(1)).findAll(pageable);
        verify(bookRepository, never()).findByTitleContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("FindAll: Should return a filtered paginated list of books when a title is provided")
    void findAll_whenTitleProvided_shouldReturnFilteredBooks() {
        // Given
        String titleFilter = "Hobbit";
        Pageable pageable = PageRequest.of(0, 10);
        Book book = new Book(UUID.randomUUID(), "The Hobbit", new Author(), "978-0345339683");
        Page<Book> bookPage = new PageImpl<>(List.of(book), pageable, 1);

        when(bookRepository.findByTitleContainingIgnoreCase(titleFilter, pageable)).thenReturn(bookPage);
        when(bookMapper.toResponse(any(Book.class))).thenReturn(new BookResponseDTO(book.getId(), book.getTitle(), null, book.getIsbn()));

        // When
        Page<BookResponseDTO> actualResponse = bookService.findAll(titleFilter, pageable);

        // Then
        assertFalse(actualResponse.isEmpty());
        assertEquals(1, actualResponse.getTotalElements());
        verify(bookRepository, never()).findAll(pageable);
        verify(bookRepository, times(1)).findByTitleContainingIgnoreCase(titleFilter, pageable);
    }
}
