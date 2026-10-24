package ec.huertos.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "garden_activity")
public class Activity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public Long communityId;
  public Long authorId;
  public String type;

  @Column(length = 4000)
  public String description;

  public LocalDate date;
  public String status;
  public int individualReward;
  public int communityReward;
}
