package ec.huertos.persistence;

import ec.huertos.domain.Garden;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GardenRepository extends JpaRepository<Garden, Long> {}
