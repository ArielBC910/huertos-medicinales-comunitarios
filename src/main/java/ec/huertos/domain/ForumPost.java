package ec.huertos.domain;

import jakarta.persistence.*;
import java.time.*;

@Entity
@Table(name = "forum_post")
public class ForumPost {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  public Long id;

  public Long communityId;
  public Long authorId;
  public Long parentId;
  public String title;

  @Column(length = 4000)
  public String body;

  public boolean hidden;
  public Instant createdAt;
}
