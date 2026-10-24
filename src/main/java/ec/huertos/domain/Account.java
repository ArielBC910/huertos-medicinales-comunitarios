package ec.huertos.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "user_account")
public class Account {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  @Column(unique = true, nullable = false)
  public String username;

  public String password;
  public String displayName;
  public String role;
  public Long communityId;
  public boolean active;
}
