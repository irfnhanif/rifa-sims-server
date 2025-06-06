package io.github.irfnhanif.rifasims.controller;

import io.github.irfnhanif.rifasims.dto.APIResponse;
import io.github.irfnhanif.rifasims.dto.RegisterRequest;
import io.github.irfnhanif.rifasims.entity.User;
import io.github.irfnhanif.rifasims.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.ws.rs.InternalServerErrorException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // jangan lupa translate response.message ke bahasa indo
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<APIResponse<String>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User registeredUser = authService.register(registerRequest);
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
        } catch (BadCredentialsException e) {
            if (e.getMessage().contains("Wait for owner approval")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new APIResponse<>(false, "Tunggu Persetujuan Pemilik (OWNER)",
                                null, List.of("Your account exists but is waiting for approval")));
            }
            throw e;
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<APIResponse<String>> refreshToken(HttpServletRequest request) {
        try {
            String token = authService.refreshToken(request);
            return ResponseEntity.ok(new APIResponse<>(true, "Refresh token successfully", token, null));
        } catch (Exception e) {
            throw new InternalServerErrorException(e.getMessage());
        }
    }
}
