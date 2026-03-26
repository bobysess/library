package com.bobysess.library.author.api;

import org.springframework.web.bind.annotation.RestController;

import com.bobysess.library.author.AuthorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping("/api/v1/authors")
@RestController
public class AuthorApiController {
    private final AuthorService authorService;
    private final AuthorMapper authorMapper;

    public AuthorApiController(AuthorService authorService, AuthorMapper authorMapper) {
        this.authorService = authorService;
        this.authorMapper = authorMapper;
    }

    @PostMapping
    public AuthorDto createAuthor(@RequestBody AuthorDto authorDto) {
        var author = authorMapper.toDomain(authorDto);
        author.setId(null); // Ensure ID is null for creation
        author = authorService.createAuthor(author);
        return authorMapper.toDto(author);
    }

    @PutMapping("/{id}")
    public AuthorDto updateAuthor(@PathVariable UUID id, @RequestBody AuthorDto authorDto) {
        var author = authorMapper.toDomain(authorDto);
        author.setId(id);
        author = authorService.updateAuthor(author);
        return authorMapper.toDto(author);
    }

    @GetMapping("/search")
    public List<AuthorDto> findByName(@RequestParam String name) {
        return authorService.findByName(name).stream()
                .map(authorMapper::toDto)
                .toList();
    }
}
