package uk.adamwoollen.events.utils;

import java.util.UUID;

import org.springframework.test.json.AbstractJsonContentAssert;

public final class JsonAssertions {
    private JsonAssertions() {
    }

    /**
     * Asserts the object at {@code path} has exactly the given fields: the field
     * count matches and every named field is present, so no unknown fields exist.
     */
    public static void hasExactlyFields(AbstractJsonContentAssert<?> json, String path, String... fields) {
        json.extractingPath(path + ".*").asList().hasSize(fields.length);
        for (var field : fields) {
            json.hasPath(path + "." + field);
        }
    }

    public static void hasUuidAt(AbstractJsonContentAssert<?> json, String path) {
        json.extractingPath(path).asString().satisfies(UUID::fromString);
    }
}
