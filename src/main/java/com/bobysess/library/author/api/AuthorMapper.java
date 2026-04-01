package com.bobysess.library.author.api;

import org.springframework.stereotype.Component;

import com.bobysess.library.author.Author;

import lombok.NoArgsConstructor;

@NoArgsConstructor
@Component
public class AuthorMapper {

    public Author toDomain(AuthorDto author) {
        return new Author(
                author.id(),
                author.firstName(),
                author.lastName(),
                author.biography(),
                author.birthDate(),
                author.deathDate());
    }

    public AuthorDto toDto(Author author) {
        return new AuthorDto(
                author.getId(),
                author.getFirstName(),
                author.getLastName(),
                author.getBiography(),
                author.getBirthDate(),
                author.getDeathDate());
    }
}
