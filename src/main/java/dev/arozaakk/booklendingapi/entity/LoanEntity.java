package dev.arozaakk.booklendingapi.entity;

import dev.arozaakk.booklendingapi.model.enums.LoanStatus;
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
@Table(name = "loan")
public class LoanEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long _id;

  @Column(name = "loan_uuid", nullable = false)
  private UUID loanUuid;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private LoanStatus status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "book_id")
  private BookEntity bookEntity;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "member_id")
  private MemberEntity memberEntity;

  @Column(name = "borrowed_utc", updatable = false)
  private LocalDateTime borrowedDateTime;

  @Column(name = "due_utc", updatable = false)
  private LocalDateTime dueDateTime;

  @Column(name = "completed_utc", updatable = false)
  private LocalDateTime completedDateTime;

  @Column(name = "created_utc", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdDateTime;

  @Column(name = "updated_utc", nullable = false, insertable = false, updatable = false)
  private LocalDateTime updatedDateTime;
}
