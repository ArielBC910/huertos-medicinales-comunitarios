package ec.huertos.persistence;

import ec.huertos.domain.PointMovement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointMovementRepository extends JpaRepository<PointMovement, Long> {}
