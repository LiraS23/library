package com.liras23.library.book.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record BookRequestDTO(
        @NotBlank(message = "Book title cannot be blank.")
        @Size(min = 2, max = 150, message = "Book title must be between 2 and 150 characters.")
        String title,

        @NotNull(message = "Author ID cannot be null.")
        UUID authorId,

        @NotBlank(message = "ISBN cannot be blank.")
        @Size(min = 10, max = 20, message = "ISBN must be between 10 and 20 characters.")
        String isbn
) {
}
