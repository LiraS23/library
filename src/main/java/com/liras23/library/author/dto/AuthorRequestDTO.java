package com.liras23.library.author.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthorRequestDTO(
        @NotBlank(message = "Author name cannot be blank.")
        @Size(min = 2, max = 100, message = "Author name must be between 2 and 100 characters.")
        String name
) {
}
