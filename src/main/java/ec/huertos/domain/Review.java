package ec.huertos.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "evidence_review")
public class Review {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public Long activityId;
  public Long submissionId;
  public Long reviewerId;
  public String decision;

  @Column(length = 4000)
  public String observations;

  public Instant createdAt;
}
