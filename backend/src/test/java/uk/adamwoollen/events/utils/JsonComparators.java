package uk.adamwoollen.events.utils;

import java.util.Arrays;
import java.util.UUID;

import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.ValueMatcher;
import org.skyscreamer.jsonassert.comparator.CustomComparator;
import org.springframework.test.json.JsonAssert;
import org.springframework.test.json.JsonComparator;

public final class JsonComparators {
    private JsonComparators() {
    }

    /**
     * Strict comparison (no extra fields, exact values) except that the given
     * paths only need to hold a parseable UUID.
     */
    public static JsonComparator strictWithUuidAt(String... paths) {
        var customizations = Arrays.stream(paths)
                .map(path -> new Customization(path, isUuid()))
                .toArray(Customization[]::new);
        return JsonAssert.comparator(new CustomComparator(JSONCompareMode.STRICT, customizations));
    }

    private static ValueMatcher<Object> isUuid() {
        return (actual, expected) -> {
            try {
                UUID.fromString(String.valueOf(actual));
                return true;
            } catch (IllegalArgumentException _) {
                return false;
            }
        };
    }
}
