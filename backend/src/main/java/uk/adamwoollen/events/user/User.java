package uk.adamwoollen.events.user;

import java.time.Instant;
import java.util.UUID;
import java.util.regex.Pattern;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import uk.adamwoollen.events.user.exception.InvalidUsernameException;
import uk.adamwoollen.events.utils.Patterns;

@Entity
@Table(name = "users")
public class User {
    public static final String VALID_USERNAME_REGEX = "^[a-zA-Z0-9_-]+$";
    public static final Pattern VALID_USERNAME_PATTERN = Pattern.compile(VALID_USERNAME_REGEX);
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 30;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false, unique = true, updatable = false)
    private UUID uuid;

    @Column(nullable = false, unique = true)
    private String username;

    protected User() {
    }

    User(UUID uuid, String username) throws InvalidUsernameException {
        this.uuid = uuid;
        setUsername(username);
    }

    public User(String username) throws InvalidUsernameException {
        this(UUID.randomUUID(), username);
    }

    public Long getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getUsername() {
        return username;
    }

    @SuppressWarnings("unused")
    public void setUsername(String username) throws InvalidUsernameException {
        if (username.length() < USERNAME_MIN_LENGTH || username.length() > USERNAME_MAX_LENGTH)
            throw new InvalidUsernameException("Username must be %d-%d characters".formatted(USERNAME_MIN_LENGTH, USERNAME_MAX_LENGTH));
        if (!VALID_USERNAME_PATTERN.matcher(username).matches())
            throw new InvalidUsernameException("Username must only contain letters, numbers, hyphens, and underscores");
        // Will compile out.  Will check is not UUID if long enough to be a valid UUID.
        if (USERNAME_MAX_LENGTH >= 36 && Patterns.UUID_PATTERN.matcher(username).matches())
            throw new InvalidUsernameException("Username must not be a valid UUID");
        this.username = username;
    }
}
