package com.liras23.library.book.controller;

import com.liras23.library.book.dto.BookRequestDTO;
import com.liras23.library.book.dto.BookResponseDTO;
import com.liras23.library.book.service.BookService;
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
@RequestMapping("/api/books")
@Tag(name = "Books", description = "Endpoints for managing books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Operation(
            summary = "Find all books",
            description = "Returns a paginated list of books. Can be filtered by title."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public ResponseEntity<Page<BookResponseDTO>> findAll(
            @RequestParam(required = false) String title,
            @ParameterObject Pageable pageable) {
        Page<BookResponseDTO> page = bookService.findAll(title, pageable);
        return ResponseEntity.ok(page);
    }

    @Operation(
            summary = "Find book by ID",
            description = "Returns a single book by its unique ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved book", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BookResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{\"error\": \"Book not found with id: 123e4567-e89b-12d3-a456-426614174000\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookResponseDTO> findById(@PathVariable UUID id) {
        BookResponseDTO responseDTO = bookService.findById(id);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(
            summary = "Create a new book",
            description = "Creates a new book and associates it with an existing author."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Book created successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BookResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{\"title\": \"Book title cannot be blank.\"}"))),
            @ApiResponse(responseCode = "404", description = "Author not found for the provided authorId", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{\"error\": \"Author not found with id: 123e4567-e89b-12d3-a456-426614174000\"}"))),
            @ApiResponse(responseCode = "409", description = "ISBN already exists", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{\"error\": \"A book with ISBN 978-0345339683 already exists.\"}")))
    })
    @PostMapping
    public ResponseEntity<BookResponseDTO> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Book data to create. The authorId must refer to an existing author.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = BookRequestDTO.class),
                            examples = @ExampleObject(value = "{\"title\": \"The Hobbit\", \"authorId\": \"c0a80121-7ac0-191b-817a-c08ab0a12345\", \"isbn\": \"978-0345339683\"}")
                    )
            )
            @Valid @RequestBody BookRequestDTO requestDTO) {
        BookResponseDTO responseDTO = bookService.create(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @Operation(
            summary = "Update an existing book",
            description = "Updates the details of an existing book by its ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Book updated successfully", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = BookResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{\"isbn\": \"ISBN cannot be blank.\"}"))),
            @ApiResponse(responseCode = "404", description = "Book or Author not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                    @ExampleObject(name = "Book not found", value = "{\"error\": \"Book not found with id: 123e4567-e89b-12d3-a456-426614174000\"}"),
                    @ExampleObject(name = "Author not found", value = "{\"error\": \"Author not found with id: c0a80121-7ac0-191b-817a-c08ab0a12345\"}")
            })),
            @ApiResponse(responseCode = "409", description = "ISBN already exists", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{\"error\": \"A book with ISBN 978-0345339683 already exists.\"}")))
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookResponseDTO> update(
            @PathVariable UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Book data to update. The authorId must refer to an existing author.",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = BookRequestDTO.class),
                            examples = @ExampleObject(value = "{\"title\": \"The Hobbit, or There and Back Again\", \"authorId\": \"c0a80121-7ac0-191b-817a-c08ab0a12345\", \"isbn\": \"978-0345339683\"}")
                    )
            )
            @Valid @RequestBody BookRequestDTO requestDTO) {
        BookResponseDTO responseDTO = bookService.update(id, requestDTO);
        return ResponseEntity.ok(responseDTO);
    }

    @Operation(
            summary = "Delete a book",
            description = "Deletes a book by its unique ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Book deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Book not found", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = @ExampleObject(value = "{\"error\": \"Book not found with id: 123e4567-e89b-12d3-a456-426614174000\"}")))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        bookService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
