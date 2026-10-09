package uk.adamwoollen.events.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static uk.adamwoollen.events.utils.JsonAssertions.hasExactlyFields;
import static uk.adamwoollen.events.utils.JsonAssertions.hasUuidAt;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import uk.adamwoollen.events.user.User;
import uk.adamwoollen.events.user.UserRepository;

@WebMvcTest(EventController.class)
public class EventControllerTest {
    @Autowired MockMvcTester mvc;

    @MockitoBean EventRepository eventRepository;
    @MockitoBean UserRepository userRepository;

    User user;

    @BeforeEach
    void createUser() {
        user = new User("tester");
    }

    @Test
    void getEventReturnsEvent() {
        var event = new Event("Test event", "A test event.", user);
        given(eventRepository.findByUuid(event.getUuid())).willReturn(Optional.of(event));

        var json = assertThat(mvc.get().uri("/event/" + event.getUuid()))
            .hasStatusOk()
            .bodyJson();

        hasExactlyFields(json, "$", "createdAt", "uuid", "title", "description", "organiserUuid");
        hasUuidAt(json, "$.uuid");
        hasUuidAt(json, "$.organiserUuid");
        json.extractingPath("$.uuid").isEqualTo(event.getUuid().toString());
        json.extractingPath("$.title").isEqualTo("Test event");
        json.extractingPath("$.description").isEqualTo("A test event.");
        json.extractingPath("$.organiserUuid").isEqualTo(user.getUuid().toString());
    }

    @Test
    void getUnknownEventReturnsNotFound() {
        var uuid = UUID.randomUUID();
        given(eventRepository.findByUuid(uuid)).willReturn(Optional.empty());

        assertThat(mvc.get().uri("/event/" + uuid))
            .hasStatus(HttpStatus.NOT_FOUND);
    }

    @Test
    void getEventWithInvalidUuidReturnsBadRequest() {
        assertThat(mvc.get().uri("/event/not-a-uuid"))
            .hasStatus(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createEventReturnsEvent() {
        given(userRepository.findByUuid(user.getUuid())).willReturn(Optional.of(user));
        given(eventRepository.save(any(Event.class))).willAnswer(returnsFirstArg());

        var json = assertThat(mvc.post()
                .uri("/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Test event",
                        "description": "A test event.",
                        "organiserUuid": "%s"
                    }
                    """.formatted(user.getUuid())))
            .hasStatusOk()
            .bodyJson();

        hasExactlyFields(json, "$", "createdAt", "uuid", "title", "description", "organiserUuid");
        hasUuidAt(json, "$.uuid");
        hasUuidAt(json, "$.organiserUuid");
        json.extractingPath("$.title").isEqualTo("Test event");
        json.extractingPath("$.description").isEqualTo("A test event.");
        json.extractingPath("$.organiserUuid").isEqualTo(user.getUuid().toString());
    }

    @Test
    void createEventWithUnknownOrganiserReturnsBadRequest() {
        var organiserUuid = UUID.randomUUID();
        given(userRepository.findByUuid(organiserUuid)).willReturn(Optional.empty());

        assertThat(mvc.post()
                .uri("/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "title": "Test event",
                        "description": "A test event.",
                        "organiserUuid": "%s"
                    }
                    """.formatted(organiserUuid)))
            .hasStatus(HttpStatus.BAD_REQUEST);
    }
}
