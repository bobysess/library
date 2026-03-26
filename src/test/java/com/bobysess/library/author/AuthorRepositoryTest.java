package com.bobysess.library.author;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class AuthorRepositoryTest {

    @Autowired
    AuthorRepository authorRepository;

    @BeforeEach
    void setUp() {
        authorRepository.save(new Author("Victor", "Hugo", "French poet and novelist",
                LocalDate.of(1802, 2, 26), LocalDate.of(1885, 5, 22)));
        authorRepository.save(new Author("Alexandre", "Dumas", "French novelist",
                LocalDate.of(1802, 7, 24), LocalDate.of(1870, 12, 5)));
    }

    @AfterEach
    void tearDown() {
        authorRepository.deleteAll();
    }

    @Test
    void findByLastnameContainingOrFirstnameContaining_matchesByLastname() {
        List<Author> results = authorRepository
                .findByName("hug");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getFullName()).isEqualTo("Victor Hugo");
    }

    @Test
    void findByLastnameContainingOrFirstnameContaining_matchesByFirstname() {
        List<Author> results = authorRepository
                .findByName("vict");

        assertThat(results).hasSize(1);
        assertThat(results.getFirst().getFullName()).isEqualTo("Victor Hugo");
    }

    @Test
    void findByLastnameContainingOrFirstnameContaining_matchesMultiple() {
        List<Author> results = authorRepository.findByName("r");

        assertThat(results)
                .hasSize(2)
                .extracting(Author::getFullName)
                .containsExactlyInAnyOrder("Victor Hugo", "Alexandre Dumas");
    }

    @Test
    void findByLastnameContainingOrFirstnameContaining_returnsEmpty_whenNoMatch() {
        List<Author> results = authorRepository
                .findByName("xyz");

        assertThat(results).isEmpty();
    }
}
