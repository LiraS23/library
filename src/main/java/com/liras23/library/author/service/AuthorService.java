package com.liras23.library.author.service;

import com.liras23.library.author.Author;
import com.liras23.library.author.AuthorRepository;
import com.liras23.library.author.dto.AuthorRequestDTO;
import com.liras23.library.author.dto.AuthorResponseDTO;
import com.liras23.library.author.mapper.AuthorMapper;
import com.liras23.library.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final AuthorMapper authorMapper;

    public AuthorService(AuthorRepository authorRepository, AuthorMapper authorMapper) {
        this.authorRepository = authorRepository;
        this.authorMapper = authorMapper;
    }

    @Transactional(readOnly = true)
    public Page<AuthorResponseDTO> findAll(String name, Pageable pageable) {
        Page<Author> page;
        if (StringUtils.hasText(name)) {
            page = authorRepository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            page = authorRepository.findAll(pageable);
        }
        return page.map(authorMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AuthorResponseDTO findById(UUID id) {
        Author author = findAuthorById(id);
        return authorMapper.toResponse(author);
    }

    @Transactional
    public AuthorResponseDTO create(AuthorRequestDTO requestDTO) {
        Author author = authorMapper.toEntity(requestDTO);
        Author savedAuthor = authorRepository.save(author);
        return authorMapper.toResponse(savedAuthor);
    }

    @Transactional
    public AuthorResponseDTO update(UUID id, AuthorRequestDTO requestDTO) {
        Author authorToUpdate = findAuthorById(id);
        authorToUpdate.setName(requestDTO.name());
        Author updatedAuthor = authorRepository.save(authorToUpdate);
        return authorMapper.toResponse(updatedAuthor);
    }

    @Transactional
    public void delete(UUID id) {
        if (!authorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Author not found with id: " + id);
        }
        authorRepository.deleteById(id);
    }

    private Author findAuthorById(UUID id) {
        return authorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Author not found with id: " + id));
    }
}
