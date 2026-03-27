package com.bobysess.library.author;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.bobysess.library.author.api.AuthorApiController;
import com.bobysess.library.author.api.AuthorMapper;

@ExtendWith(MockitoExtension.class)
class AuthorApiControllerTest {

    @Mock
    private AuthorService authorService;

    private final AuthorMapper authorMapper = new AuthorMapper();

    private AuthorApiController authorApiController;

    private RestTestClient restTestClient;

    @BeforeEach
    void setUp() {
        authorApiController = new AuthorApiController(authorService, authorMapper);
        restTestClient = RestTestClient.bindToController(authorApiController).build();
    }

    @Test
    void createAuthor_savesAuthorAndReturnsDto() {
        var savedId = UUID.randomUUID();
        var savedAuthor = new Author(savedId, "Jane", "Austen", "English novelist.",
                LocalDate.of(1775, 12, 16), LocalDate.of(1817, 7, 18));

        when(authorService.createAuthor(any(Author.class))).thenReturn(savedAuthor);

        restTestClient.post().uri("/api/v1/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "firstName": "Jane",
                            "lastName": "Austen",
                            "biography": "English novelist.",
                            "birthDate": "1775-12-16",
                            "deathDate": "1817-07-18"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("""
                        {
                            "id": "%s",
                            "firstName": "Jane",
                            "lastName": "Austen",
                            "biography": "English novelist.",
                            "birthDate": "1775-12-16",
                            "deathDate": "1817-07-18"
                        }
                        """.formatted(savedId));

        verify(authorService).createAuthor(any(Author.class));
    }

    @Test
    void createAuthors_savesAuthorsAndReturnsDtoList() {
        var savedFirstId = UUID.randomUUID();
        var savedSecondId = UUID.randomUUID();
        var savedAuthors = List.of(
                new Author(savedFirstId, "Jane", "Austen", "English novelist.",
                        LocalDate.of(1775, 12, 16), LocalDate.of(1817, 7, 18)),
                new Author(savedSecondId, "Mark", "Twain", "American writer.",
                        LocalDate.of(1835, 11, 30), LocalDate.of(1910, 4, 21)));

        when(authorService.createAuthors(any())).thenReturn(savedAuthors);

        restTestClient.post().uri("/api/v1/authors/man")
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        [
                            {
                                "firstName": "Jane",
                                "lastName": "Austen",
                                "biography": "English novelist.",
                                "birthDate": "1775-12-16",
                                "deathDate": "1817-07-18"
                            },
                            {
                                "firstName": "Mark",
                                "lastName": "Twain",
                                "biography": "American writer.",
                                "birthDate": "1835-11-30",
                                "deathDate": "1910-04-21"
                            }
                        ]
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("""
                        [
                            {
                                "id": "%s",
                                "firstName": "Jane",
                                "lastName": "Austen",
                                "biography": "English novelist.",
                                "birthDate": "1775-12-16",
                                "deathDate": "1817-07-18"
                            },
                            {
                                "id": "%s",
                                "firstName": "Mark",
                                "lastName": "Twain",
                                "biography": "American writer.",
                                "birthDate": "1835-11-30",
                                "deathDate": "1910-04-21"
                            }
                        ]
                        """.formatted(savedFirstId, savedSecondId));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Author>> captor = ArgumentCaptor.forClass(List.class);
        verify(authorService).createAuthors(captor.capture());
        List<Author> capturedAuthors = captor.getValue();
        assertThat(capturedAuthors)
                .hasSize(2)
                .extracting(Author::getFirstName)
                .containsExactly("Jane", "Mark");
    }

    @Test
    void updateAuthor_updatesAuthorAndReturnsDto() {
        var id = UUID.randomUUID();
        var updatedAuthor = new Author(id, "Charles", "Dickens", "Victorian author.",
                LocalDate.of(1812, 2, 7), LocalDate.of(1870, 6, 9));

        when(authorService.updateAuthor(any(Author.class))).thenReturn(updatedAuthor);

        restTestClient.put().uri("/api/v1/authors/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .body("""
                        {
                            "firstName": "Charles",
                            "lastName": "Dickens",
                            "biography": "Victorian author.",
                            "birthDate": "1812-02-07",
                            "deathDate": "1870-06-09"
                        }
                        """)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("""
                        {
                            "id": "%s",
                            "firstName": "Charles",
                            "lastName": "Dickens",
                            "biography": "Victorian author.",
                            "birthDate": "1812-02-07",
                            "deathDate": "1870-06-09"
                        }
                        """.formatted(id));

        verify(authorService).updateAuthor(any(Author.class));
    }

    @Test
    void findByName_returnsListOfMatchingAuthors() {
        var author = new Author(UUID.randomUUID(), "Mark", "Twain", "American writer.",
                LocalDate.of(1835, 11, 30), LocalDate.of(1910, 4, 21));

        when(authorService.findByName("Twain")).thenReturn(List.of(author));

        restTestClient.get().uri("/api/v1/authors/search?name=Twain")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .json("""
                        [
                            {
                                "id": "%s",
                                "firstName": "Mark",
                                "lastName": "Twain",
                                "biography": "American writer.",
                                "birthDate": "1835-11-30",
                                "deathDate": "1910-04-21"
                            }
                        ]
                        """.formatted(author.getId()));

        verify(authorService).findByName("Twain");
    }

}
