package uk.adamwoollen.events.utils;

import java.util.regex.Pattern;

public class Patterns {
    public static final String UUID_REGEX = "(?i)[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";
    public static final Pattern UUID_PATTERN = Pattern.compile(UUID_REGEX);

    private Patterns() {}
}
