package io.github.irfnhanif.rifasims.controller;

import io.github.irfnhanif.rifasims.entity.User;
import io.github.irfnhanif.rifasims.entity.UserRole;
import io.github.irfnhanif.rifasims.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    UserRepository userRepository;

    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("")
    public void createUser(@RequestBody User user) {

    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("")
    public ResponseEntity<Iterable<User>> getAllUsers(@RequestParam(required = false) Integer branch,
                                                      @RequestParam(required = false)UserRole role) {
    }

    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUser(@PathVariable UUID userId) {

    }

    @PreAuthorize("hasRole('OWNER')")
    @PutMapping("/{userId}")
    public void updateUser(@PathVariable UUID userId, @RequestBody User user) {

    }

    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable UUID userId) {

    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser() {

    }

    @PutMapping("/me")
    public void updateCurrentUser(@RequestBody User user) {

    }
}
