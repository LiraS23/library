package com.liras23.library.author.mapper;

import com.liras23.library.author.Author;
import com.liras23.library.author.dto.AuthorRequestDTO;
import com.liras23.library.author.dto.AuthorResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class AuthorMapper {

    public Author toEntity(AuthorRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        Author author = new Author();
        author.setName(dto.name());
        return author;
    }

    public AuthorResponseDTO toResponse(Author entity) {
        if (entity == null) {
            return null;
        }
        return new AuthorResponseDTO(entity.getId(), entity.getName());
    }
}
