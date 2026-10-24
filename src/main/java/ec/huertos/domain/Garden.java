package ec.huertos.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "garden")
public class Garden {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public Long communityId;
  public String name;
  public String address;
  public double latitude;
  public double longitude;
  public double area;
  public String design;
  public String soil;
  public String water;
  public String compost;
  public String fence;
}
