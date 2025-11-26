package com.liras23.library.author.service;

import com.liras23.library.author.Author;
import com.liras23.library.author.AuthorRepository;
import com.liras23.library.author.dto.AuthorRequestDTO;
import com.liras23.library.author.dto.AuthorResponseDTO;
import com.liras23.library.author.mapper.AuthorMapper;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @Mock
    private AuthorMapper authorMapper;

    @InjectMocks
    private AuthorService authorService;

    @Test
    @DisplayName("Should throw ResourceNotFoundException when author is not found")
    void findById_whenAuthorNotFound_shouldThrowResourceNotFoundException() {
        // Given
        UUID authorId = UUID.randomUUID();
        when(authorRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            authorService.findById(authorId);
        });
    }

    @Test
    @DisplayName("Should return author when author is found")
    void findById_whenAuthorFound_shouldReturnAuthor() {
        // Given
        UUID authorId = UUID.randomUUID();
        Author authorEntity = new Author(authorId, "J.R.R. Tolkien");
        AuthorResponseDTO expectedResponse = new AuthorResponseDTO(authorId, "J.R.R. Tolkien");

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(authorEntity));
        when(authorMapper.toResponse(authorEntity)).thenReturn(expectedResponse);

        // When
        AuthorResponseDTO actualResponse = authorService.findById(authorId);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.id(), actualResponse.id());
        assertEquals(expectedResponse.name(), actualResponse.name());
    }

    @Test
    @DisplayName("Should create a new author successfully")
    void create_shouldCreateNewAuthor() {
        // Given
        AuthorRequestDTO requestDTO = new AuthorRequestDTO("George Orwell");
        Author authorToSave = new Author(null, "George Orwell"); // Entidade antes de salvar (sem ID)
        Author savedAuthor = new Author(UUID.randomUUID(), "George Orwell"); // Entidade depois de salvar (com ID)
        AuthorResponseDTO expectedResponse = new AuthorResponseDTO(savedAuthor.getId(), savedAuthor.getName());

        when(authorMapper.toEntity(requestDTO)).thenReturn(authorToSave);
        when(authorRepository.save(authorToSave)).thenReturn(savedAuthor);
        when(authorMapper.toResponse(savedAuthor)).thenReturn(expectedResponse);

        // When
        AuthorResponseDTO actualResponse = authorService.create(requestDTO);

        // Then
        assertNotNull(actualResponse);
        assertNotNull(actualResponse.id());
        assertEquals(expectedResponse.name(), actualResponse.name());
    }

    @Test
    @DisplayName("Should update author successfully when author exists")
    void update_whenAuthorExists_shouldUpdateAuthor() {
        // Given
        UUID authorId = UUID.randomUUID();
        AuthorRequestDTO requestDTO = new AuthorRequestDTO("J.R.R. Tolkien");
        Author existingAuthor = new Author(authorId, "John Ronald Reuel Tolkien"); // Dados antigos
        Author updatedAuthor = new Author(authorId, "J.R.R. Tolkien"); // Dados novos
        AuthorResponseDTO expectedResponse = new AuthorResponseDTO(authorId, "J.R.R. Tolkien");

        when(authorRepository.findById(authorId)).thenReturn(Optional.of(existingAuthor));
        when(authorRepository.save(any(Author.class))).thenReturn(updatedAuthor);
        when(authorMapper.toResponse(updatedAuthor)).thenReturn(expectedResponse);

        // When
        AuthorResponseDTO actualResponse = authorService.update(authorId, requestDTO);

        // Then
        assertNotNull(actualResponse);
        assertEquals(expectedResponse.id(), actualResponse.id());
        assertEquals(expectedResponse.name(), actualResponse.name());
        verify(authorRepository, times(1)).findById(authorId);
        verify(authorRepository, times(1)).save(any(Author.class));
    }

    @Test
    @DisplayName("Should return a paginated list of authors when no name is provided")
    void findAll_whenNoNameProvided_shouldReturnAllAuthors() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Author author = new Author(UUID.randomUUID(), "J.R.R. Tolkien");
        Page<Author> authorPage = new PageImpl<>(List.of(author), pageable, 1);

        when(authorRepository.findAll(pageable)).thenReturn(authorPage);
        when(authorMapper.toResponse(any(Author.class))).thenReturn(new AuthorResponseDTO(author.getId(), author.getName()));

        // When
        Page<AuthorResponseDTO> actualResponse = authorService.findAll(null, pageable);

        // Then
        assertNotNull(actualResponse);
        assertEquals(1, actualResponse.getTotalElements());
        assertEquals(1, actualResponse.getContent().size());
        verify(authorRepository, times(1)).findAll(pageable);
        verify(authorRepository, never()).findByNameContainingIgnoreCase(anyString(), any(Pageable.class));
    }

    @Test
    @DisplayName("Should return a filtered paginated list of authors when a name is provided")
    void findAll_whenNameProvided_shouldReturnFilteredAuthors() {
        // Given
        String nameFilter = "Tolkien";
        Pageable pageable = PageRequest.of(0, 10);
        Author author = new Author(UUID.randomUUID(), "J.R.R. Tolkien");
        Page<Author> authorPage = new PageImpl<>(List.of(author), pageable, 1);

        when(authorRepository.findByNameContainingIgnoreCase(nameFilter, pageable)).thenReturn(authorPage);
        when(authorMapper.toResponse(any(Author.class))).thenReturn(new AuthorResponseDTO(author.getId(), author.getName()));

        // When
        Page<AuthorResponseDTO> actualResponse = authorService.findAll(nameFilter, pageable);

        // Then
        assertNotNull(actualResponse);
        assertEquals(1, actualResponse.getTotalElements());
        assertEquals("J.R.R. Tolkien", actualResponse.getContent().get(0).name());
        verify(authorRepository, never()).findAll(pageable);
        verify(authorRepository, times(1)).findByNameContainingIgnoreCase(nameFilter, pageable);
    }

    @Test
    @DisplayName("Should delete author successfully when author exists")
    void delete_whenAuthorExists_shouldDeleteAuthor() {
        // Given
        UUID authorId = UUID.randomUUID();
        when(authorRepository.existsById(authorId)).thenReturn(true);
        doNothing().when(authorRepository).deleteById(authorId);

        // When
        authorService.delete(authorId);

        // Then
        verify(authorRepository, times(1)).existsById(authorId);
        verify(authorRepository, times(1)).deleteById(authorId);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when trying to delete a non-existing author")
    void delete_whenAuthorDoesNotExist_shouldThrowResourceNotFoundException() {
        // Given
        UUID authorId = UUID.randomUUID();
        when(authorRepository.existsById(authorId)).thenReturn(false);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            authorService.delete(authorId);
        });

        verify(authorRepository, times(1)).existsById(authorId);
        verify(authorRepository, never()).deleteById(authorId);
    }
}
