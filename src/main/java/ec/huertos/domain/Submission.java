package ec.huertos.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "submission")
public class Submission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public Long activityId;
  public Long authorId;

  @Column(length = 4000)
  public String description;

  public String photoKey;
  public Instant createdAt;
}
