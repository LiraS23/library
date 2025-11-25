package com.liras23.library.author.controller;

import com.liras23.library.author.dto.AuthorRequestDTO;
import com.liras23.library.author.dto.AuthorResponseDTO;
import com.liras23.library.author.service.AuthorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/authors")
@Tag(name = "Authors", description = "Endpoints for managing authors")
public class AuthorController {

    private final AuthorService authorService;

    public AuthorController(AuthorService authorService) {
        this.authorService = authorService;
    }

    @Operation(
            summary = "Find all authors",
            description = "Returns a paginated list of authors. Can be filtered by name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public ResponseEntity<Page<AuthorResponseDTO>> findAll(
            @RequestParam(required = false) String name,
            @ParameterObject Pageable pageable) {
        Page<AuthorResponseDTO> page = authorService.findAll(name, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(
            summary = "Find author by ID",
            description = "Returns a single author by its unique ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved author", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthorResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Author not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{\"error\": \"Author not found with id: 123e4567-e89b-12d3-a456-426614174000\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AuthorResponseDTO> findById(@PathVariable UUID id) {
        AuthorResponseDTO responseDTO = authorService.findById(id);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(
            summary = "Create a new author",
            description = "Creates a new author with the provided name."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Author created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{\"name\": \"Author name must be between 2 and 100 characters.\"}")))
    })
    @PostMapping
    public ResponseEntity<AuthorResponseDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Author data to create.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AuthorRequestDTO.class),
                            examples = @ExampleObject(value = "{\"name\": \"J.K. Rowling\"}")
                    )
            )
            @Valid @RequestBody AuthorRequestDTO requestDTO) {
        AuthorResponseDTO responseDTO = authorService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(
            summary = "Update an existing author",
            description = "Updates the name of an existing author by its ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Author updated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthorResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{\"name\": \"Author name cannot be blank.\"}"))),
            @ApiResponse(responseCode = "404", description = "Author not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{\"error\": \"Author not found with id: 123e4567-e89b-12d3-a456-426614174000\"}")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<AuthorResponseDTO> update(
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Author data to update.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = AuthorRequestDTO.class),
                            examples = @ExampleObject(value = "{\"name\": \"J.R.R. Tolkien\"}")
                    )
            )
            @Valid @RequestBody AuthorRequestDTO requestDTO) {
        AuthorResponseDTO responseDTO = authorService.update(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(
            summary = "Delete an author",
            description = "Deletes an author by its unique ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Author deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Author not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{\"error\": \"Author not found with id: 123e4567-e89b-12d3-a456-426614174000\"}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        authorService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
