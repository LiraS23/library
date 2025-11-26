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
        BookRequestDTO requestDTO = new BookRequestDTO("The Hobbit", existingAuthor.getId(), "978-0345339683");

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value(requestDTO.title()))
                .andExpect(jsonPath("$.author.id").value(existingAuthor.getId().toString()));
    }

    @Test
    @DisplayName("Create: Should return 400 Bad Request for invalid data (blank title)")
    void create_whenTitleIsBlank_shouldReturnBadRequest() throws Exception {
        BookRequestDTO requestDTO = new BookRequestDTO("", existingAuthor.getId(), "978-0345339683");

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    @DisplayName("Create: Should return 404 Not Found when author does not exist")
    void create_whenAuthorNotFound_shouldReturnNotFound() throws Exception {
        BookRequestDTO requestDTO = new BookRequestDTO("The Hobbit", UUID.randomUUID(), "978-0345339683");

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Create: Should return 409 Conflict when ISBN already exists")
    void create_whenIsbnExists_shouldReturnConflict() throws Exception {
        bookRepository.save(new Book(null, "Existing Book", existingAuthor, "978-0345339683"));
        BookRequestDTO requestDTO = new BookRequestDTO("The Hobbit", existingAuthor.getId(), "978-0345339683");

        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("FindById: Should return a book and 200 OK when ID exists")
    void findById_whenIdExists_shouldReturnBook() throws Exception {
        Book book = bookRepository.save(new Book(null, "The Silmarillion", existingAuthor, "978-0618391110"));

        mockMvc.perform(get(API_URL + "/{id}", book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(book.getId().toString()))
                .andExpect(jsonPath("$.title").value(book.getTitle()));
    }

    @Test
    @DisplayName("FindById: Should return 404 Not Found when ID does not exist")
    void findById_whenIdDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(get(API_URL + "/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("FindAll: Should return a paginated list of books")
    void findAll_shouldReturnPaginatedList() throws Exception {
        bookRepository.save(new Book(null, "The Lord of the Rings", existingAuthor, "978-0618640157"));
        bookRepository.save(new Book(null, "The Hobbit", existingAuthor, "978-0345339683"));

        mockMvc.perform(get(API_URL)
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "title,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("The Hobbit")))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("FindAll: Should return a filtered list when title parameter is provided")
    void findAll_whenTitleIsProvided_shouldReturnFilteredList() throws Exception {
        bookRepository.save(new Book(null, "The Lord of the Rings", existingAuthor, "978-0618640157"));
        bookRepository.save(new Book(null, "The Hobbit", existingAuthor, "978-0345339683"));

        mockMvc.perform(get(API_URL)
                        .param("title", "Hobbit"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("The Hobbit")));
    }

    @Test
    @DisplayName("Update: Should update a book and return 200 OK when ID exists")
    void update_whenIdExists_shouldUpdateBook() throws Exception {
        Book book = bookRepository.save(new Book(null, "Old Title", existingAuthor, "978-0000000111"));
        BookRequestDTO requestDTO = new BookRequestDTO("New Title", existingAuthor.getId(), "978-0000000222");

        mockMvc.perform(put(API_URL + "/{id}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"))
                .andExpect(jsonPath("$.isbn").value("978-0000000222"));
    }

    @Test
    @DisplayName("Update: Should return 404 Not Found when ID does not exist")
    void update_whenIdDoesNotExist_shouldReturnNotFound() throws Exception {
        BookRequestDTO requestDTO = new BookRequestDTO("New Title", existingAuthor.getId(), "978-0000000222");

        mockMvc.perform(put(API_URL + "/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update: Should return 404 Not Found when Author ID does not exist")
    void update_whenAuthorIdDoesNotExist_shouldReturnNotFound() throws Exception {
        Book book = bookRepository.save(new Book(null, "Old Title", existingAuthor, "978-0000000111"));
        BookRequestDTO requestDTO = new BookRequestDTO("New Title", UUID.randomUUID(), "978-0000000222");

        mockMvc.perform(put(API_URL + "/{id}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update: Should return 409 Conflict when ISBN already belongs to another book")
    void update_whenIsbnExistsInAnotherBook_shouldReturnConflict() throws Exception {
        Book bookToUpdate = bookRepository.save(new Book(null, "Original Book", existingAuthor, "978-0000000111"));
        Book otherBook = bookRepository.save(new Book(null, "Other Book", existingAuthor, "978-0000000222"));
        BookRequestDTO requestDTO = new BookRequestDTO("Updated Title", existingAuthor.getId(), otherBook.getIsbn());

        mockMvc.perform(put(API_URL + "/{id}", bookToUpdate.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Delete: Should delete a book and return 204 No Content when ID exists")
    void delete_whenIdExists_shouldDeleteBook() throws Exception {
        Book book = bookRepository.save(new Book(null, "To be deleted", existingAuthor, "978-0000000555"));

        mockMvc.perform(delete(API_URL + "/{id}", book.getId()))
                .andExpect(status().isNoContent());

        assertFalse(bookRepository.existsById(book.getId()));
    }

    @Test
    @DisplayName("Delete: Should return 404 Not Found when ID does not exist")
    void delete_whenIdDoesNotExist_shouldReturnNotFound() throws Exception {
        mockMvc.perform(delete(API_URL + "/{id}", UUID.randomUUID()))
                .andExpect(status().isNotFound());
    }
}
