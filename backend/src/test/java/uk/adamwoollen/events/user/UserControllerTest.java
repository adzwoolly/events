package uk.adamwoollen.events.user;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;


import static uk.adamwoollen.events.utils.JsonAssertions.hasExactlyFields;
import static uk.adamwoollen.events.utils.JsonAssertions.hasUuidAt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    UserRepository userRepository;

    @Test
    void listUsersListsAllUsers() {
        var users = List.of(
            new User(UUID.randomUUID(), "tester"),
            new User(UUID.randomUUID(), "tester2"),
            new User(UUID.randomUUID(), "tester3")
        );
        given(userRepository.findAll()).willReturn(users);

        var json = assertThat(mvc.get().uri("/user"))
            .hasStatusOk()
            .bodyJson();

        json.extractingPath("$").asInstanceOf(InstanceOfAssertFactories.LIST).hasSize(users.size());
        for (var i = 0; i < users.size(); i++) {
            var user = "$[" + i + "]";
            hasExactlyFields(json, user, "uuid", "username");
            hasUuidAt(json, user + ".uuid");
            json.extractingPath(user + ".username").isEqualTo(users.get(i).getUsername());
        }
    }

    @Test
    void createUserReturnsUser() {
        given(userRepository.save(any(User.class))).willAnswer(returnsFirstArg());

        var json = assertThat(mvc.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "tester"
                    }
                    """))
            .hasStatusOk()
            .bodyJson();

        hasExactlyFields(json, "$", "uuid", "username");
        hasUuidAt(json, "$.uuid");
        json.extractingPath("$.username").isEqualTo("tester");
    }

    @Test
    void cannotCreateDuplicateUser() {
        given(userRepository.save(any(User.class))).willThrow(new DataIntegrityViolationException("duplicate username"));

        assertThat(mvc.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "tester"
                    }
                    """))
            .hasStatus(HttpStatus.CONFLICT);
    }

    @Test
    void getUserByUuid() {
        var uuidString = "1-2-3-4-5";
        var uuid = UUID.fromString(uuidString);
        given(userRepository.findByUuid(uuid)).willReturn(Optional.of(new User(uuid, "tester")));

        var json = assertThat(mvc.get().uri("/user/" + uuidString))
            .hasStatusOk()
            .bodyJson();

        hasExactlyFields(json, "$", "uuid", "username");
        hasUuidAt(json, "$.uuid");
        json.extractingPath("$.username").isEqualTo("tester");
    }
}
