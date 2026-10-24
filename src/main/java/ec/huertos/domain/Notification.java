package ec.huertos.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "notification")
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public Long accountId;

  @Column(length = 4000)
  public String message;

  public boolean seen;
  public Instant createdAt;
}
