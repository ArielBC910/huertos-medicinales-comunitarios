package ec.huertos.persistence;

import ec.huertos.domain.ContentReport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {}
