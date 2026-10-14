package uk.adamwoollen.events.user;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import uk.adamwoollen.events.user.exception.InvalidUsernameException;
import uk.adamwoollen.events.user.exception.UsernameAlreadyExistsException;
import uk.adamwoollen.events.utils.Patterns;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByUuidOrUsername(String uuidOrUsername) {
        return Patterns.UUID_PATTERN.matcher(uuidOrUsername).matches()
            ? userRepository.findByUuid(UUID.fromString(uuidOrUsername))
            : userRepository.findByUsername(uuidOrUsername);
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public User createUser(String username) throws UsernameAlreadyExistsException, InvalidUsernameException {
        try {
            return userRepository.saveAndFlush(new User(username));
        } catch (DataIntegrityViolationException _) {
            throw new UsernameAlreadyExistsException("A user with this username already exists");
        }
    }
}
