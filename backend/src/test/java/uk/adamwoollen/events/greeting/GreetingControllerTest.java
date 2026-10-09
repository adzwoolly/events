package uk.adamwoollen.events.greeting;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@WebMvcTest(GreetingController.class)
public class GreetingControllerTest {
    @Autowired
    MockMvcTester mvc;

    @MockitoBean
    GreetingIdGenerator idGenerator;

    @Test
    void greetsByName() {
        given(idGenerator.next()).willReturn(1L);

        assertThat(mvc.get().uri("/greeting").param("name", "Tester"))
            .hasStatusOk()
            .bodyJson()
            .withResourceLoadClass(GreetingController.class)
            .isStrictlyEqualTo("response-named-greeting.json");
    }

    @Test
    void greetsDefaultName() {
        given(idGenerator.next()).willReturn(1L);

        assertThat(mvc.get().uri("/greeting"))
            .hasStatusOk()
            .bodyJson()
            .isStrictlyEqualTo("{\"id\": 1, \"content\": \"Hello, World!\"}");
    }
}
