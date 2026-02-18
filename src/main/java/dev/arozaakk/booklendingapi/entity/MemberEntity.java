package dev.arozaakk.booklendingapi.entity;

import dev.arozaakk.booklendingapi.model.enums.MemberStatus;
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
@Table(name = "member")
public class MemberEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long _id;

  @Column(name = "member_uuid", nullable = false)
  private UUID memberUuid;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "email", nullable = false)
  private String email;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private MemberStatus status;

  @Column(name = "created_utc", nullable = false, insertable = false, updatable = false)
  private LocalDateTime createdDateTime;

  @Column(name = "updated_utc", nullable = false, insertable = false, updatable = false)
  private LocalDateTime updatedDateTime;
}
