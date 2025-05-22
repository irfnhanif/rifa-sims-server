package io.github.irfnhanif.rifasims.controller;

import io.github.irfnhanif.rifasims.dto.APIResponse;
import io.github.irfnhanif.rifasims.entity.User;
import io.github.irfnhanif.rifasims.service.AuthService;
import jakarta.validation.Valid;
import jakarta.ws.rs.InternalServerErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<APIResponse<String>> register(@Valid @RequestBody User user) {
        try {
            User registeredUser = authService.register(user);
            String registerMessage = String.format("Registered user: %s", registeredUser.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(new APIResponse<>(true, "Register successfully, Please wait confirmation from owner", registerMessage, null));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<APIResponse<String>> login(@RequestBody User user) {
        try{
            String token = authService.login(user.getUsername(), user.getPassword());
            return ResponseEntity.ok(new APIResponse<>(true, "Login successfully", token, null));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
