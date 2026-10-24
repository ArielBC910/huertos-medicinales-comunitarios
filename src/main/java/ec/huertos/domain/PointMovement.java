package ec.huertos.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(
    name = "point_movement",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "beneficiary_key"}))
public class PointMovement {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public Long activityId;
  public Long communityId;
  public Long accountId;
  public String beneficiaryKey;
  public int amount;
  public Instant createdAt;
}
