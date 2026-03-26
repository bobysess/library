package com.bobysess.library.author.api;

import java.time.LocalDate;
import java.util.UUID;

public record AuthorDto(UUID id, String firstName, String lastName, String biography, LocalDate birthDate,
        LocalDate deathDate) {

}
