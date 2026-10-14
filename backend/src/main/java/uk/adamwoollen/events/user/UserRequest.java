package uk.adamwoollen.events.user;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserRequest(
    @NotNull(message = "Username is required")
    @Size(min = User.USERNAME_MIN_LENGTH, max = User.USERNAME_MAX_LENGTH, message = "Username must be {min}-{max} characters")
    @Pattern(regexp = User.VALID_USERNAME_REGEX, message = "Username must only contain letters, numbers, hyphens, and underscores")
    String username) {
}
