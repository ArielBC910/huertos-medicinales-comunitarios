package ec.huertos.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "reviewer_assignment")
public class ReviewerAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public Long reviewerId;
  public Long communityId;
}
