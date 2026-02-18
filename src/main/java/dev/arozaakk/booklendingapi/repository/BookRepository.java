package dev.arozaakk.booklendingapi.repository;

import dev.arozaakk.booklendingapi.entity.BookEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, Long> {
  Optional<BookEntity> findFirstByBookUuid(UUID bookUuid);
}
