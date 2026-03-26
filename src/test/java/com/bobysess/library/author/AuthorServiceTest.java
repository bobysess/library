package com.bobysess.library.author;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthorServiceTest {

    @Mock
    private AuthorRepository authorRepository;

    @InjectMocks
    private AuthorService authorService;

    @Test
    void createAuthor_savesAndReturnsAuthor() {
        // given
        LocalDate birthDate = LocalDate.of(1775, 12, 16);
        LocalDate deathDate = LocalDate.of(1817, 7, 18);
        Author saved = new Author(UUID.randomUUID(), "Jane", "Austen", "English novelist", birthDate, deathDate);
        when(authorRepository.save(any(Author.class))).thenReturn(saved);

        // when
        Author result = authorService.createAuthor("Jane", "Austen", "English novelist", birthDate, deathDate);

        // then
        assertThat(result).isEqualTo(saved);
        verify(authorRepository).save(any(Author.class));
    }

    @Test
    void updateAuthor_savesAndReturnsAuthor() {
        // given
        Author author = new Author(UUID.randomUUID(), "Mark", "Twain", "American author",
                LocalDate.of(1835, 11, 30), LocalDate.of(1910, 4, 21));
        when(authorRepository.save(author)).thenReturn(author);

        // when
        Author result = authorService.updateAuthor(author);

        // then
        assertThat(result).isEqualTo(author);
        verify(authorRepository).save(author);
    }

    @Test
    void findByName_delegatesToRepository() {
        // given
        List<Author> expected = List.of(
                new Author(UUID.randomUUID(), "Victor", "Hugo", null, LocalDate.of(1802, 2, 26), null));
        when(authorRepository.findByName("Hugo")).thenReturn(expected);

        // when
        List<Author> result = authorService.findByName("Hugo");

        // then
        assertThat(result).isEqualTo(expected);
        verify(authorRepository).findByName("Hugo");
    }

    @Test
    void validateAuthor_validAuthor_doesNotThrow() {
        Author validAuthor = new Author("Leo", "Tolstoy", "Russian novelist",
                LocalDate.of(1828, 9, 9), LocalDate.of(1910, 11, 20));

        // no exception expected
        assertDoesNotThrow(() -> authorService.updateAuthor(validAuthor));
    }

    static Stream<Arguments> invalidAuthorCases() {
        LocalDate past = LocalDate.of(1900, 1, 1);
        LocalDate future = LocalDate.now().plusDays(1);

        return Stream.of(
                Arguments.of(
                        new Author(null, "Doe", null, null, null),
                        InvalidAuthorException.Reason.MISSING_FIRST_NAME),
                Arguments.of(
                        new Author("John", null, null, null, null),
                        InvalidAuthorException.Reason.MISSING_LAST_NAME),
                Arguments.of(
                        new Author("John", "Doe", null, future, null),
                        InvalidAuthorException.Reason.INVALID_BIRTH_DATE),
                Arguments.of(
                        new Author("John", "Doe", null, past, future),
                        InvalidAuthorException.Reason.INVALID_DEATH_DATE),
                Arguments.of(
                        new Author("John", "Doe", null, LocalDate.of(2000, 1, 1), LocalDate.of(1990, 1, 1)),
                        InvalidAuthorException.Reason.DEATH_BEFORE_BIRTH));
    }

    @ParameterizedTest
    @MethodSource("invalidAuthorCases")
    void validateAuthor_invalidCases_throwsInvalidAuthorException(
            Author author, InvalidAuthorException.Reason expectedReason) {
        assertThatThrownBy(() -> authorService.updateAuthor(author))
                .isInstanceOf(InvalidAuthorException.class)
                .extracting(ex -> ((InvalidAuthorException) ex).getReason())
                .isEqualTo(expectedReason);
    }
}
