package uk.adamwoollen.events.greeting;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;

@WebMvcTest
public class GreetingControllerTest {
    @Autowired
    MockMvcTester mvc;

    @Test
    void greetsByName() {
        assertThat(mvc.get().uri("/greeting").param("name", "Tester"))
            .hasStatusOk()
            .bodyJson()
            .withResourceLoadClass(GreetingController.class)
            .isStrictlyEqualTo("response-named-greeting.json");
    }
}
