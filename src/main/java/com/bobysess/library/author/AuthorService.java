package com.bobysess.library.author;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class AuthorService {
    private final AuthorRepository authorRepository;

    public AuthorService(AuthorRepository authorRepository) {
        this.authorRepository = authorRepository;
    }

    public Author createAuthor(Author author) {
        author.setId(null);
        validateAuthor(author);
        return authorRepository.save(author);
    }

    public List<Author> createAuthors(List<Author> authors) {
        authors.forEach(author -> {
            author.setId(null);
            validateAuthor(author);
        });
        return (List<Author>) authorRepository.saveAll(authors);
    }

    public Author updateAuthor(Author author) {
        if (author.getId() == null) {
            throw new IllegalArgumentException("Author id must not be null when updating");
        }
        if (!authorRepository.existsById(author.getId())) {
            throw new IllegalArgumentException("Author with id " + author.getId() + " does not exist");
        }
        validateAuthor(author);
        return authorRepository.save(author);
    }

    public List<Author> findByName(String name) {
        return authorRepository.findByName(name);
    }

    private void validateAuthor(Author author) {
        if (author.getFirstName() == null || author.getFirstName().isBlank()) {
            throw new InvalidAuthorException(InvalidAuthorException.Reason.MISSING_FIRST_NAME,
                    "First name is required");
        }
        if (author.getLastName() == null || author.getLastName().isBlank()) {
            throw new InvalidAuthorException(InvalidAuthorException.Reason.MISSING_LAST_NAME,
                    "Last name is required");
        }
        if (author.getBirthDate() != null && author.getBirthDate().isAfter(LocalDate.now())) {
            throw new InvalidAuthorException(InvalidAuthorException.Reason.INVALID_BIRTH_DATE,
                    "Birth date cannot be in the future");
        }
        if (author.getDeathDate() != null && author.getDeathDate().isAfter(LocalDate.now())) {
            throw new InvalidAuthorException(InvalidAuthorException.Reason.INVALID_DEATH_DATE,
                    "Death date cannot be in the future");
        }
        if (author.getBirthDate() != null && author.getDeathDate() != null
                && author.getDeathDate().isBefore(author.getBirthDate())) {
            throw new InvalidAuthorException(InvalidAuthorException.Reason.DEATH_BEFORE_BIRTH,
                    "Death date cannot be before birth date");
        }
    }
}
