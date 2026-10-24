package ec.huertos.persistence;

import ec.huertos.domain.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select e from Activity e where e.id = :id")
  java.util.Optional<Activity> lockById(
      @org.springframework.data.repository.query.Param("id") Long id);
}
