package io.github.irfnhanif.rifasims.controller;

import io.github.irfnhanif.rifasims.dto.APIResponse;
import io.github.irfnhanif.rifasims.dto.EditUserRequest;
import io.github.irfnhanif.rifasims.dto.UserWithTokenResponse;
import io.github.irfnhanif.rifasims.entity.User;
import io.github.irfnhanif.rifasims.security.CustomUserDetailsService;
import io.github.irfnhanif.rifasims.service.UserService;
import io.github.irfnhanif.rifasims.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAuthority('OWNER')")
public class UserController {
    // jangan lupa translate response.message ke bahasa indo
    private final UserService userService;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, CustomUserDetailsService userDetailsService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("")
    public ResponseEntity<APIResponse<List<User>>> getAllUsers(@RequestParam(required = false) String name,
                                                               @RequestParam(required = false, defaultValue = "0") Integer page,
                                                               @RequestParam(required = false, defaultValue = "10") Integer size) {
        List<User> users = userService.getAllUsers(name, page, size);
        return ResponseEntity.ok(new APIResponse<>(true, "Successfully retrieved users", users, null ));
    }

    @GetMapping("/pending")
    public ResponseEntity<APIResponse<List<User>>> getPendingUsers() {
        List<User> users = userService.getPendingUsers();
        return ResponseEntity.ok(new APIResponse<>(true, "Pending users retrieved successfully", users, null));
    }
    // kurang getById dan delete

    @GetMapping("/username/{username}")
    public ResponseEntity<APIResponse<User>> getUserByUserName(@PathVariable("username") String username) {
        User user =  userService.getUserByUsername(username);
        return ResponseEntity.ok(new APIResponse<>(true, "Successfully retrieved user", user, null));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<APIResponse<UserWithTokenResponse>> updateUser(@PathVariable UUID userId, @RequestBody EditUserRequest editUserRequest) {
        User updatedUser = userService.updateUser(userId, editUserRequest);

        UserDetails userDetails = (UserDetails) userDetailsService.loadUserByUsername(updatedUser.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        UserWithTokenResponse response = new UserWithTokenResponse(updatedUser, token);

        return ResponseEntity.ok(new APIResponse<>(true, "User updated successfully", response, null));
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

    @DeleteMapping("/{userId}")
    public ResponseEntity<APIResponse<Void>> deleteUser(@PathVariable UUID userId) {
        userService.deleteUser(userId);

        return ResponseEntity.ok(new APIResponse<>(true, "User deleted successfully", null, null));
    }
}
