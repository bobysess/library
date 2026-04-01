package com.bobysess.library.author;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface AuthorRepository extends CrudRepository<Author, UUID> {

  @Query(
      "SELECT * FROM author WHERE first_name ILIKE '%' || :name || '%' OR last_name ILIKE '%' ||"
          + " :name || '%'")
  List<Author> findByName(String name);
}
