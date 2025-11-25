package com.liras23.library.book.mapper;

import com.liras23.library.author.mapper.AuthorMapper;
import com.liras23.library.book.Book;
import com.liras23.library.book.dto.BookRequestDTO;
import com.liras23.library.book.dto.BookResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    private final AuthorMapper authorMapper;

    public BookMapper(AuthorMapper authorMapper) {
        this.authorMapper = authorMapper;
    }

    public Book toEntity(BookRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        Book book = new Book();
        book.setTitle(dto.title());
        book.setIsbn(dto.isbn());
        // A entidade Author será associada no Service
        return book;
    }

    public BookResponseDTO toResponse(Book entity) {
        if (entity == null) {
            return null;
        }
        return new BookResponseDTO(
                entity.getId(),
                entity.getTitle(),
                authorMapper.toResponse(entity.getAuthor()),
                entity.getIsbn()
        );
    }
}
