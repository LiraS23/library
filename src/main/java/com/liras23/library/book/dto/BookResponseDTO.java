package com.liras23.library.book.dto;

import com.liras23.library.author.dto.AuthorResponseDTO;

import java.util.UUID;

public record BookResponseDTO(
        UUID id,
        String title,
        AuthorResponseDTO author,
        String isbn
) {
}
