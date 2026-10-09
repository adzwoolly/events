package uk.adamwoollen.events.event;

import java.time.Instant;
import java.util.UUID;

public record EventResponse(
        Instant createdAt,
        UUID uuid,
        String title,
        String description,
        UUID organiserUuid) {
    public static EventResponse from(Event event) {
        return new EventResponse(
                event.getCreatedAt(),
                event.getUuid(),
                event.getTitle(),
                event.getDescription(),
                event.getOrganiser().getUuid());
    }
}
