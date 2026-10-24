package ec.huertos.persistence;

import ec.huertos.domain.Community;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityRepository extends JpaRepository<Community, Long> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select e from Community e where e.id = :id")
  java.util.Optional<Community> lockById(
      @org.springframework.data.repository.query.Param("id") Long id);
}
