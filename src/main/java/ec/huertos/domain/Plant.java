package ec.huertos.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "plant")
public class Plant {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public Long communityId;
  public String name;
  public String scientificName;
  public String otherNames;
  public String cycle;

  @Column(length = 4000)
  public String traditionalUse;

  @Column(length = 4000)
  public String source;
}
