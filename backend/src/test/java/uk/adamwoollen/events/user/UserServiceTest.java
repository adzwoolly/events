package uk.adamwoollen.events.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import uk.adamwoollen.events.user.exception.InvalidUsernameException;
import uk.adamwoollen.events.user.exception.UsernameAlreadyExistsException;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void listUsersListsAllUsers() throws InvalidUsernameException {
        var users = List.of(
            new User(UUID.randomUUID(), "tester"),
            new User(UUID.randomUUID(), "tester2"),
            new User(UUID.randomUUID(), "tester3")
        );
        given(userRepository.findAll()).willReturn(users);

        assertThat(userService.listUsers())
            .size().isEqualTo(3).returnToIterable()
            .isEqualTo(users);
    }

    @Test
    void createUserCreatesAndReturnsUser() throws InvalidUsernameException, UsernameAlreadyExistsException {
        given(userRepository.saveAndFlush(any(User.class))).willAnswer(returnsFirstArg());

        var user = userService.createUser("tester");
        assertThat(user.getUsername()).isEqualTo("tester");
        assertThat(user.getUuid()).isNotNull();
    }

    @Test
    void cannotCreateDuplicateUser() {
        given(userRepository.saveAndFlush(any(User.class))).willThrow(new DataIntegrityViolationException("duplicate username"));

        assertThatThrownBy(() -> userService.createUser("tester"))
            .isInstanceOf(UsernameAlreadyExistsException.class)
            .hasMessage("A user with this username already exists");
    }

    @Test
    void cannotCreateUserWithShortUsername() {
        assertThatThrownBy(() -> userService.createUser(""))
            .isInstanceOf(InvalidUsernameException.class)
            .hasMessage("Username must be 3-30 characters");

        verifyNoInteractions(userRepository);
    }

    @Test
    void cannotCreateUserWithLongUsername() {
        assertThatThrownBy(() -> userService.createUser("a".repeat(31)))
            .isInstanceOf(InvalidUsernameException.class)
            .hasMessage("Username must be 3-30 characters");

        verifyNoInteractions(userRepository);
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @ValueSource(strings = {
        // SQL injection
        "admin'--",
        "' OR '1'='1",
        "robert'); DROP TABLE users;--",
        "user\" OR \"1\"=\"1",
        "a;b",
        "a/*comment*/b",
        "100%_wildcard",
        // XSS / HTML
        "<script>alert(1)</script>",
        "<img src=x onerror=a>",
        "john<b>doe",
        "a&amp;b",
        // Path traversal / URL-significant characters
        "../../etc/passwd",
        "..\\..\\windows",
        "user/name",
        "user?name=x",
        "user#fragment",
        "user%2Fname",
        "user%00name",
        // Template / expression / command injection
        "${jndi:ldap://x/a}",
        "{{7*7}}",
        "#{7*7}",
        "$(whoami)",
        "`whoami`",
        "a|b",
        "a&&b",
        "a>b",
        // JSON / LDAP / NoSQL
        "{\"$ne\":null}",
        "a\"b",
        "*)(uid=*",
        // Whitespace and control characters
        "user name",
        "user\tname",
        "user\nname",
        "user\r\nname",
        "user\u0000name",
        "user​name",
        // Unicode / non-ASCII letters
        "üser_name",
        "用户名称测试",
        "emoji😀name",
        "caféname",
        "ｕｓｅｒｎａｍｅ",
        // Other punctuation
        "user@example.com",
        "user.name",
        "user+name",
        "user=name",
        "user:name",
        "user,name",
        "user!name",
        "user~name",
        "user\\name",
        "(username)",
        "[username]"
    })
    void cannotCreateUserWithInvalidCharacters(String username) {
        assertThatThrownBy(() -> userService.createUser(username))
            .isInstanceOf(InvalidUsernameException.class)
            .hasMessage("Username must only contain letters, numbers, hyphens, and underscores");

        verifyNoInteractions(userRepository);
    }

    @Test
    void cannotCreateUserWithUuidUsername() {
        assertThatThrownBy(() -> userService.createUser("123e4567-e89b-12d3-a456-426614174000"))
            .isInstanceOf(InvalidUsernameException.class)
            .message().isIn(
                "Username must be 3-30 characters",  // If the max length is smaller than UUID length of 36
                "Username must not be a valid UUID"
            );

        verifyNoInteractions(userRepository);
    }

    @Test
    void findByUuidOrUsernameFindsUserByUuid() throws InvalidUsernameException {
        var uuidString = "44402cb2-0a72-4267-a528-c4d9cfcf9e40";
        var uuid = UUID.fromString(uuidString);
        var user = new User(uuid, "tester");
        given(userRepository.findByUuid(uuid)).willReturn(Optional.of(user));

        assertThat(userService.findByUuidOrUsername(uuidString)).contains(user);

        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    void findByUuidOrUsernameFindsUserByUsername() throws InvalidUsernameException {
        var user = new User("tester");
        given(userRepository.findByUsername("tester")).willReturn(Optional.of(user));

        assertThat(userService.findByUuidOrUsername("tester")).contains(user);

        verify(userRepository, never()).findByUuid(any());
    }

    @Test
    void findByUuidOrUsernameReturnsEmptyWhenNotFound() {
        given(userRepository.findByUsername("missing")).willReturn(Optional.empty());

        assertThat(userService.findByUuidOrUsername("missing")).isEmpty();
    }
}

