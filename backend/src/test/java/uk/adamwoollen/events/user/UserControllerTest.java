package uk.adamwoollen.events.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import uk.adamwoollen.events.utils.JsonComparators;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import java.util.UUID;

@WebMvcTest(UserController.class)
public class UserControllerTest {
    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    UserRepository userRepository;

    @Test
    void createUserReturnsUser() {
        given(userRepository.save(any(User.class))).willAnswer(returnsFirstArg());

        assertThat(mvc.post()
                .uri("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "tester"
                    }
                    """))
            .hasStatusOk()
            .bodyJson()
            .isEqualTo("""
                {
                    "uuid": "ignored",
                    "username": "tester"
                }
                """, JsonComparators.strictWithUuidAt("uuid"));
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

        assertThat(mvc.get().uri("/user/" + uuidString))
            .hasStatusOk()
            .bodyJson()
            .isEqualTo("""
                {
                    "uuid": "ignored",
                    "username": "tester"
                }
                """, JsonComparators.strictWithUuidAt("uuid"));
    }
}
