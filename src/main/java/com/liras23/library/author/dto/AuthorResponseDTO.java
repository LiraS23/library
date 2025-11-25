package com.liras23.library.author.dto;

import java.util.UUID;

public record AuthorResponseDTO(
        UUID id,
        String name
) {
}
