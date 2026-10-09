package uk.adamwoollen.events.user;

import java.util.UUID;

public record UserResponse(UUID uuid, String username) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getUuid(), user.getUsername());
    }
}
