package uk.adamwoollen.events.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static uk.adamwoollen.events.utils.JsonAssertions.hasErrorBody;
import static uk.adamwoollen.events.utils.JsonAssertions.hasExactlyFields;
import static uk.adamwoollen.events.utils.JsonAssertions.hasUuidAt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import uk.adamwoollen.events.user.exception.InvalidUsernameException;
import uk.adamwoollen.events.user.exception.UsernameAlreadyExistsException;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    UserService userService;

    @Test
    void getUsersReturnsUsersJson200() throws InvalidUsernameException {
        var users = List.of(
            new User(UUID.randomUUID(), "tester"),
            new User(UUID.randomUUID(), "tester2"),
            new User(UUID.randomUUID(), "tester3")
        );
        given(userService.listUsers()).willReturn(users);

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
    void createUserReturnsCreatedUserJson200() throws InvalidUsernameException, UsernameAlreadyExistsException {
        given(userService.createUser(any(String.class))).willAnswer(x -> new User(x.getArgument(0)));

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
    void createUserWithoutUsernameReturns400ErrorWithoutCallingService() throws Exception {
        var json = assertThat(mvc.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson();

        hasErrorBody(json, HttpStatus.BAD_REQUEST, "Username is required");
        verify(userService, never()).createUser(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "ab", // too short
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa" // 31 chars, too long
    })
    void createUserWithInvalidLengthReturnsError400WithoutCallingService(String username) throws Exception {
        var json = assertThat(mvc.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "%s"
                    }
                    """.formatted(username)))
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson();

        hasErrorBody(json, HttpStatus.BAD_REQUEST, "Username must be 3-30 characters");
        verify(userService, never()).createUser(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "has space",
        "bad!name"
    })
    void createUserWithInvalidCharactersReturnsError400WithoutCallingService(String username) throws Exception {
        var json = assertThat(mvc.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "%s"
                    }
                    """.formatted(username)))
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson();

        hasErrorBody(json, HttpStatus.BAD_REQUEST, "Username must only contain letters, numbers, hyphens, and underscores");
        verify(userService, never()).createUser(any());
    }

    @Test
    void createUserReturnsError400WhenServiceThrowsInvalidUsername() throws Exception {
        given(userService.createUser("tester"))
            .willThrow(new InvalidUsernameException("Username must not be a valid UUID"));

        var json = assertThat(mvc.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    { "username": "tester" }
                    """))
            .hasStatus(HttpStatus.BAD_REQUEST)
            .bodyJson();

        hasErrorBody(json, HttpStatus.BAD_REQUEST, "Username must not be a valid UUID");
    }

    @Test
    void createUserReturnsError409WhenServiceThrowsUsernameAlreadyExists() throws InvalidUsernameException, UsernameAlreadyExistsException {
        given(userService.createUser(any(String.class))).willThrow(new UsernameAlreadyExistsException(null));

        var json = assertThat(mvc.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "tester"
                    }
                    """))
            .hasStatus(HttpStatus.CONFLICT)
            .bodyJson();

        hasErrorBody(json, HttpStatus.CONFLICT, "User with username already exists");
    }

    @Test
    void getUserByUuidReturnsUserFromServiceJson200() throws InvalidUsernameException {
        var uuid = "44402cb2-0a72-4267-a528-c4d9cfcf9e40";
        given(userService.findByUuidOrUsername(uuid)).willReturn(Optional.of(new User(UUID.fromString("44402cb2-0a72-4267-a528-c4d9cfcf9e40"), "tester")));

        var json = assertThat(mvc.get().uri("/user/" + uuid))
            .hasStatusOk()
            .bodyJson();

        hasExactlyFields(json, "$", "uuid", "username");
        json.extractingPath("$.uuid").isEqualTo(uuid);
        json.extractingPath("$.username").isEqualTo("tester");
    }

    @Test
    void getUserByUsernameReturnsUserFromServiceJson200() throws InvalidUsernameException {
        var username = "tester";
        given(userService.findByUuidOrUsername(username)).willReturn(Optional.of(new User("tester")));

        var json = assertThat(mvc.get().uri("/user/" + username))
            .hasStatusOk()
            .bodyJson();

        hasExactlyFields(json, "$", "uuid", "username");
        hasUuidAt(json, "$.uuid");
        json.extractingPath("$.username").isEqualTo("tester");
    }
}
