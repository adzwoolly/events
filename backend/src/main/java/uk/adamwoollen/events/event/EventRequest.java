package uk.adamwoollen.events.event;

import java.util.UUID;

public record EventRequest(String title, String description, UUID organiserUuid) {
}
