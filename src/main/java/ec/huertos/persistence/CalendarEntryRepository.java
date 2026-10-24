package ec.huertos.persistence;

import ec.huertos.domain.CalendarEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CalendarEntryRepository extends JpaRepository<CalendarEntry, Long> {}
