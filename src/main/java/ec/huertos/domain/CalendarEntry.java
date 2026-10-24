package ec.huertos.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "calendar_entry")
public class CalendarEntry {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public LocalDate date;
  public String phase;

  @Column(length = 4000)
  public String suggestion;

  @Column(length = 4000)
  public String source;
}
