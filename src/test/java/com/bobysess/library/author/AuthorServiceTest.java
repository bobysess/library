package com.bobysess.library.author;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
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

  @Mock private AuthorRepository authorRepository;

  @InjectMocks private AuthorService sut;

  @Test
  void createAuthor_savesAndReturnsAuthor() {
    // given
    LocalDate birthDate = LocalDate.of(1775, 12, 16);
    LocalDate deathDate = LocalDate.of(1817, 7, 18);
    Author saved =
        new Author(UUID.randomUUID(), "Jane", "Austen", "English novelist", birthDate, deathDate);
    when(authorRepository.save(any(Author.class))).thenReturn(saved);

    // when
    Author result =
        sut.createAuthor(
            new Author(null, "Jane", "Austen", "English novelist", birthDate, deathDate));

    // then
    assertThat(result).isEqualTo(saved);
    verify(authorRepository).save(any(Author.class));
  }

  @Test
  void createAuthors_resetsIdsSavesAllAndReturnsAuthors() {
    // given
    Author first =
        new Author(
            UUID.randomUUID(),
            "Jane",
            "Austen",
            "English novelist",
            LocalDate.of(1775, 12, 16),
            LocalDate.of(1817, 7, 18));
    Author second =
        new Author(
            UUID.randomUUID(),
            "Mark",
            "Twain",
            "American author",
            LocalDate.of(1835, 11, 30),
            LocalDate.of(1910, 4, 21));
    List<Author> authors = List.of(first, second);
    List<Author> saved =
        List.of(
            new Author(
                UUID.randomUUID(),
                "Jane",
                "Austen",
                "English novelist",
                LocalDate.of(1775, 12, 16),
                LocalDate.of(1817, 7, 18)),
            new Author(
                UUID.randomUUID(),
                "Mark",
                "Twain",
                "American author",
                LocalDate.of(1835, 11, 30),
                LocalDate.of(1910, 4, 21)));

    when(authorRepository.saveAll(anyIterable())).thenReturn(saved);

    // when
    List<Author> result = sut.createAuthors(authors);

    // then
    assertThat(result).isEqualTo(saved);
    assertThat(authors).allSatisfy(author -> assertThat(author.getId()).isNull());
    verify(authorRepository).saveAll(authors);
  }

  @Test
  void updateAuthor_savesAndReturnsAuthor() {
    // given
    var authorId = UUID.randomUUID();
    Author author =
        new Author(
            authorId,
            "Mark",
            "Twain",
            "American author",
            LocalDate.of(1835, 11, 30),
            LocalDate.of(1910, 4, 21));
    when(authorRepository.save(author)).thenReturn(author);
    when(authorRepository.existsById(authorId)).thenReturn(true);

    // when
    Author result = sut.updateAuthor(author);

    // then
    assertThat(result).isEqualTo(author);
    verify(authorRepository).save(author);
  }

  @Test
  void updateAuthor_nullId_throwsIllegalArgumentException() {
    // given
    Author author =
        new Author(
            null,
            "Mark",
            "Twain",
            "American author",
            LocalDate.of(1835, 11, 30),
            LocalDate.of(1910, 4, 21));

    // when / then
    assertThatThrownBy(() -> sut.updateAuthor(author))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Author id must not be null when updating");
  }

  @Test
  void updateAuthor_nonExistentId_throwsIllegalArgumentException() {
    // given
    var authorId = UUID.randomUUID();
    Author author =
        new Author(
            authorId,
            "Mark",
            "Twain",
            "American author",
            LocalDate.of(1835, 11, 30),
            LocalDate.of(1910, 4, 21));
    when(authorRepository.existsById(authorId)).thenReturn(false);

    // when / then
    assertThatThrownBy(() -> sut.updateAuthor(author))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Author with id " + authorId + " does not exist");
  }

  @Test
  void findByName_delegatesToRepository() {
    // given
    List<Author> expected =
        List.of(
            new Author(UUID.randomUUID(), "Victor", "Hugo", null, LocalDate.of(1802, 2, 26), null));
    when(authorRepository.findByName("Hugo")).thenReturn(expected);

    // when
    List<Author> result = sut.findByName("Hugo");

    // then
    assertThat(result).isEqualTo(expected);
    verify(authorRepository).findByName("Hugo");
  }

  @Test
  void validateAuthor_validAuthor_doesNotThrow() {
    var authorId = UUID.randomUUID();
    Author validAuthor =
        new Author(
            authorId,
            "Mark",
            "Twain",
            "American author",
            LocalDate.of(1828, 9, 9),
            LocalDate.of(1910, 11, 20));
    when(authorRepository.existsById(authorId)).thenReturn(true);

    // no exception expected
    assertDoesNotThrow(() -> sut.updateAuthor(validAuthor));
  }

  static Stream<Arguments> invalidAuthorCases() {
    LocalDate past = LocalDate.of(1900, 1, 1);
    LocalDate future = LocalDate.now().plusDays(1);
    var id = UUID.randomUUID();

    return Stream.of(
        Arguments.of(
            new Author(id, null, "Doe", null, null, null),
            InvalidAuthorException.Reason.MISSING_FIRST_NAME),
        Arguments.of(
            new Author(id, "John", null, null, null, null),
            InvalidAuthorException.Reason.MISSING_LAST_NAME),
        Arguments.of(
            new Author(id, "John", "Doe", null, future, null),
            InvalidAuthorException.Reason.INVALID_BIRTH_DATE),
        Arguments.of(
            new Author(id, "John", "Doe", null, past, future),
            InvalidAuthorException.Reason.INVALID_DEATH_DATE),
        Arguments.of(
            new Author(id, "John", "Doe", null, LocalDate.of(2000, 1, 1), LocalDate.of(1990, 1, 1)),
            InvalidAuthorException.Reason.DEATH_BEFORE_BIRTH));
  }

  @ParameterizedTest
  @MethodSource("invalidAuthorCases")
  void validateAuthor_invalidCases_throwsInvalidAuthorException(
      Author author, InvalidAuthorException.Reason expectedReason) {
    when(authorRepository.existsById(author.getId())).thenReturn(true);

    assertThatThrownBy(() -> sut.updateAuthor(author))
        .isInstanceOf(InvalidAuthorException.class)
        .extracting(ex -> ((InvalidAuthorException) ex).getReason())
        .isEqualTo(expectedReason);
  }
}
