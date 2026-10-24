package ec.huertos.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "content_report")
public class ContentReport {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public Long postId;
  public Long reporterId;
  public String reason;
  public String status;
  public Long moderatorId;
  public String resolution;
  public Instant createdAt;
}
