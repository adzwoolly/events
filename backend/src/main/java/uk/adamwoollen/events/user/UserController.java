package uk.adamwoollen.events.user;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;
import uk.adamwoollen.events.user.exception.InvalidUsernameException;
import uk.adamwoollen.events.user.exception.UsernameAlreadyExistsException;


@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public List<UserResponse> listUsers() {
        return userService.listUsers().stream()
            .map(UserResponse::from)
            .toList();
    }

    @GetMapping("/{userUuidOrUsername}")
    public UserResponse getUser(@PathVariable String userUuidOrUsername) {
        return userService.findByUuidOrUsername(userUuidOrUsername)
            .map(UserResponse::from)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping()
    public UserResponse createUser(@Valid @RequestBody UserRequest request) {
        try {
            var user = userService.createUser(request.username());
            return UserResponse.from(user);
        } catch (InvalidUsernameException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (UsernameAlreadyExistsException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with username already exists", e);
        }
    }
}
