package io.github.irfnhanif.rifasims.controller;

import io.github.irfnhanif.rifasims.dto.APIResponse;
import io.github.irfnhanif.rifasims.entity.User;
import io.github.irfnhanif.rifasims.entity.UserRole;
import io.github.irfnhanif.rifasims.repository.UserRepository;
import io.github.irfnhanif.rifasims.service.UserService;
import jakarta.ws.rs.InternalServerErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAuthority('OWNER')")
public class UserController {
    // jangan lupa translate response.message ke bahasa indo
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/pending")
    public ResponseEntity<APIResponse<List<User>>> getPendingUsers() {
        List<User> users = userService.getPendingUsers();
        return ResponseEntity.ok(new APIResponse<>(true, "Pending users retrieved successfully", users, null));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<APIResponse<User>> updateUser(@PathVariable UUID userId, @RequestBody User user) {
        User updatedUser = userService.updateUser(userId, user);
        return ResponseEntity.ok(new APIResponse<>(true, "User updated successfully", updatedUser, null));
    }

    @PatchMapping("/{userId}/accept")
    public ResponseEntity<APIResponse<String>> acceptUser(@PathVariable UUID userId) {
        User acceptedUser = userService.acceptUser(userId);
        String response = String.format("User %s accepted successfully", acceptedUser.getUsername());
        return ResponseEntity.ok(new APIResponse<>(true, "User accepted successfully", response, null));
    }

    @PatchMapping("/{userId}/reject")
    public ResponseEntity<APIResponse<String>> rejectUser(@PathVariable UUID userId) {
        User rejectedUser = userService.rejectUser(userId);
        String response = String.format("User %s rejected successfully", rejectedUser.getUsername());
        return ResponseEntity.ok(new APIResponse<>(true, "User rejected successfully", response, null));
    }
}
