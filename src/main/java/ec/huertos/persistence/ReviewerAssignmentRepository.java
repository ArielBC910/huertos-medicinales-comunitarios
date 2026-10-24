package ec.huertos.persistence;

import ec.huertos.domain.ReviewerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewerAssignmentRepository extends JpaRepository<ReviewerAssignment, Long> {}
