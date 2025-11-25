package com.liras23.library.author.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liras23.library.author.Author;
import com.liras23.library.author.AuthorRepository;
import com.liras23.library.author.dto.AuthorRequestDTO;
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
class AuthorControllerTest {

    private static final String API_URL = "/api/authors";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthorRepository authorRepository;

    @Test
    @DisplayName("Create: Should create a new author and return 201 Created")
    void create_whenValidData_shouldReturnCreated() throws Exception {
        // Given
        AuthorRequestDTO requestDTO = new AuthorRequestDTO("J.K. Rowling");

        // When & Then
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value(requestDTO.name()));
    }

    @Test
    @DisplayName("Create: Should return 400 Bad Request when data is invalid")
    void create_whenInvalidData_shouldReturnBadRequest() throws Exception {
        // Given
        AuthorRequestDTO requestDTO = new AuthorRequestDTO(""); // Blank name violates @NotBlank and @Size

        // When & Then
        mockMvc.perform(post(API_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name", anyOf(
                        is("Author name cannot be blank."),
                        is("Author name must be between 2 and 100 characters.")
                )));
    }

    @Test
    @DisplayName("FindById: Should return an author and 200 OK when ID exists")
    void findById_whenIdExists_shouldReturnAuthor() throws Exception {
        // Given
        Author author = authorRepository.save(new Author(null, "J.R.R. Tolkien"));

        // When & Then
        mockMvc.perform(get(API_URL + "/{id}", author.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(author.getId().toString()))
                .andExpect(jsonPath("$.name").value(author.getName()));
    }

    @Test
    @DisplayName("FindById: Should return 404 Not Found when ID does not exist")
    void findById_whenIdDoesNotExist_shouldReturnNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(get(API_URL + "/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("FindAll: Should return a paginated list of authors")
    void findAll_shouldReturnPaginatedList() throws Exception {
        // Given
        authorRepository.save(new Author(null, "George Orwell"));
        authorRepository.save(new Author(null, "J.R.R. Tolkien"));

        // When & Then
        mockMvc.perform(get(API_URL)
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("George Orwell")))
                .andExpect(jsonPath("$.totalPages", is(2)))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("Update: Should update an author and return 200 OK when ID exists")
    void update_whenIdExists_shouldUpdateAuthor() throws Exception {
        // Given
        Author author = authorRepository.save(new Author(null, "George Martin"));
        AuthorRequestDTO requestDTO = new AuthorRequestDTO("George R. R. Martin");

        // When & Then
        mockMvc.perform(put(API_URL + "/{id}", author.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(author.getId().toString()))
                .andExpect(jsonPath("$.name").value(requestDTO.name()));
    }

    @Test
    @DisplayName("Update: Should return 404 Not Found when ID does not exist")
    void update_whenIdDoesNotExist_shouldReturnNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        AuthorRequestDTO requestDTO = new AuthorRequestDTO("Valid Name");

        // When & Then
        mockMvc.perform(put(API_URL + "/{id}", nonExistentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Delete: Should delete an author and return 204 No Content when ID exists")
    void delete_whenIdExists_shouldDeleteAuthor() throws Exception {
        // Given
        Author author = authorRepository.save(new Author(null, "To be deleted"));

        // When & Then
        mockMvc.perform(delete(API_URL + "/{id}", author.getId()))
                .andExpect(status().isNoContent());

        assertFalse(authorRepository.existsById(author.getId()));
    }

    @Test
    @DisplayName("Delete: Should return 404 Not Found when ID does not exist")
    void delete_whenIdDoesNotExist_shouldReturnNotFound() throws Exception {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When & Then
        mockMvc.perform(delete(API_URL + "/{id}", nonExistentId))
                .andExpect(status().isNotFound());
    }
}
