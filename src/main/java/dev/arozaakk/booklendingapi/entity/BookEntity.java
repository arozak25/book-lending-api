package dev.arozaakk.booklendingapi.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "book")
public class BookEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long _id;

  @Column(name = "book_uuid", nullable = false)
  private UUID bookUuid;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "author", nullable = false)
  private String author;

  @Column(name = "isbn", nullable = false)
  private String isbn;

  @Column(name = "total_copies", nullable = false)
  private Long totalCopies;

  @Column(name = "available_copies", nullable = false)
  private Long availableCopies;

  @Column(name = "created_utc", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdDateTime;

  @Column(name = "updated_utc", nullable = false, insertable = false, updatable = false)
  private LocalDateTime updatedDateTime;
}
