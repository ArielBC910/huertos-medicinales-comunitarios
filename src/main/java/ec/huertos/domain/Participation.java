package ec.huertos.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(
    name = "participation",
    uniqueConstraints = @UniqueConstraint(columnNames = {"activity_id", "account_id"}))
public class Participation {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public Long activityId;
  public Long accountId;
}
