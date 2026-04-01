package com.bobysess.library.author;

import java.time.LocalDate;
import java.util.UUID;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table("author")
public class Author {
  @Id private UUID id;
  private String firstName;
  private String lastName;
  private String biography;
  private LocalDate birthDate;
  private LocalDate deathDate;

  /** Reconstitution constructor — used by Spring Data JDBC when loading from DB. */
  @PersistenceCreator
  public Author(
      UUID id,
      String firstName,
      String lastName,
      String biography,
      LocalDate birthDate,
      LocalDate deathDate) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.biography = biography;
    this.birthDate = birthDate;
    this.deathDate = deathDate;
  }

  /** Creation constructor — leaves {@code id} null so the database generates it on insert. */
  public Author(
      String firstName,
      String lastName,
      String biography,
      LocalDate birthDate,
      LocalDate deathDate) {
    this.firstName = firstName;
    this.lastName = lastName;
    this.biography = biography;
    this.birthDate = birthDate;
    this.deathDate = deathDate;
  }

  public String getFullName() {
    return String.format("%s %s", firstName, lastName);
  }
}
