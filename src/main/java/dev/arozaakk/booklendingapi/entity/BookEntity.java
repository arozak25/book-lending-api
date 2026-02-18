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

  @Column(name = "book_uuid")
  private UUID bookUuid;

  @Column(name = "title")
  private String title;

  @Column(name = "author")
  private String author;

  @Column(name = "isbn")
  private String isbn;

  @Column(name = "total_copies")
  private Long totalCopies;

  @Column(name = "available_copies")
  private Long availableCopies;

  @Column(name = "created_utc", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdDateTime;

  @Column(name = "updated_utc", nullable = false, insertable = false, updatable = false)
  private LocalDateTime updatedDateTime;
}
