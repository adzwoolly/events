package uk.adamwoollen.events.event;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import uk.adamwoollen.events.user.UserService;

@RestController
@RequestMapping("/event")
public class EventController {
    private final EventRepository eventRepository;
    private final UserService userService;

    public EventController(EventRepository eventRepository, UserService userService) {
        this.eventRepository = eventRepository;
        this.userService = userService;
    }

    @PostMapping
    public EventResponse createEvent(@RequestBody EventRequest request) {
        var organiser = userService.findByUuid(request.organiserUuid())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Organiser not found"));
        var event = eventRepository.save(new Event(request.title(), request.description(), organiser));
        return EventResponse.from(event);
    }

    @GetMapping("/{eventUuid}")
    public EventResponse getEvent(@PathVariable UUID eventUuid) {
        return eventRepository.findByUuid(eventUuid)
            .map(EventResponse::from)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }
}
