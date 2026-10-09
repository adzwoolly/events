package uk.adamwoollen.events.event;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findByUuid(UUID uuid);
}
