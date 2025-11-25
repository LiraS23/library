package com.liras23.library.book.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liras23.library.author.Author;
import com.liras23.library.author.AuthorRepository;
import com.liras23.library.book.Book;
import com.liras23.library.book.BookRepository;
import com.liras23.library.book.dto.BookRequestDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Transactional
class BookControllerTest {

    private static final String API_URL = "/api/books";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private AuthorRepository authorRepository;

    private Author existingAuthor;

    @BeforeEach
    void setUp() {
        existingAuthor = authorRepository.save(new Author(null, "J.R.R. Tolkien"));
    }

    @Test
    @DisplayName("Create: Should create a new book and return 201 Created")
    void create_whenValidData_shouldReturnCreated() throws Exception {
        // Given
        BookRequestDTO requestDTO = new BookRequestDTO("The Hobbit", existingAuthor.getId(), "978-0345339683");

        // When & Then
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value(requestDTO.title()))
                .andExpect(jsonPath("$.author.id").value(existingAuthor.getId().toString()));
    }

    @Test
    @DisplayName("Create: Should return 404 Not Found when author does not exist")
    void create_whenAuthorNotFound_shouldReturnNotFound() throws Exception {
        // Given
        BookRequestDTO requestDTO = new BookRequestDTO("The Hobbit", UUID.randomUUID(), "978-0345339683");

        // When & Then
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Create: Should return 409 Conflict when ISBN already exists")
    void create_whenIsbnExists_shouldReturnConflict() throws Exception {
        // Given
        bookRepository.save(new Book(null, "Existing Book", existingAuthor, "978-0345339683"));
        BookRequestDTO requestDTO = new BookRequestDTO("The Hobbit", existingAuthor.getId(), "978-0345339683");

        // When & Then
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("FindById: Should return a book and 200 OK when ID exists")
    void findById_whenIdExists_shouldReturnBook() throws Exception {
        // Given
        Book book = bookRepository.save(new Book(null, "The Silmarillion", existingAuthor, "978-0618391110"));

        // When & Then
        mockMvc.perform(get(API_URL + "/{id}", book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(book.getId().toString()))
                .andExpect(jsonPath("$.title").value(book.getTitle()));
    }

    @Test
    @DisplayName("FindById: Should return 404 Not Found when ID does not exist")
    void findById_whenIdDoesNotExist_shouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(get(API_URL + "/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("FindAll: Should return a paginated list of books")
    void findAll_shouldReturnPaginatedList() throws Exception {
        // Given
        bookRepository.save(new Book(null, "Book A", existingAuthor, "1111111111"));
        bookRepository.save(new Book(null, "Book B", existingAuthor, "2222222222"));

        // When & Then
        mockMvc.perform(get(API_URL)
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Book A")))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("Update: Should update a book and return 200 OK when ID exists")
    void update_whenIdExists_shouldUpdateBook() throws Exception {
        // Given
        Book book = bookRepository.save(new Book(null, "Old Title", existingAuthor, "3333333333"));
        BookRequestDTO requestDTO = new BookRequestDTO("New Title", existingAuthor.getId(), "4444444444");

        // When & Then
        mockMvc.perform(put(API_URL + "/{id}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.isbn").value("4444444444"));
    }

    @Test
    @DisplayName("Update: Should return 404 Not Found when ID does not exist")
    void update_whenIdDoesNotExist_shouldReturnNotFound() throws Exception {
        // Given
        BookRequestDTO requestDTO = new BookRequestDTO("New Title", existingAuthor.getId(), "4444444444");

        // When & Then
        mockMvc.perform(put(API_URL + "/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete: Should delete a book and return 204 No Content when ID exists")
    void delete_whenIdExists_shouldDeleteBook() throws Exception {
        // Given
        Book book = bookRepository.save(new Book(null, "To be deleted", existingAuthor, "5555555555"));

        // When & Then
        mockMvc.perform(delete(API_URL + "/{id}", book.getId()))
                .andExpect(status().isNoContent());

        assertFalse(bookRepository.existsById(book.getId()));
    }

    @Test
    @DisplayName("Delete: Should return 404 Not Found when ID does not exist")
    void delete_whenIdDoesNotExist_shouldReturnNotFound() throws Exception {
        // When & Then
        mockMvc.perform(delete(API_URL + "/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
