package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.dto.RegisterRequest;
import io.github.irfnhanif.rifasims.entity.User;
import io.github.irfnhanif.rifasims.entity.UserRole;
import io.github.irfnhanif.rifasims.entity.UserStatus;
import io.github.irfnhanif.rifasims.exception.BadRequestException;
import io.github.irfnhanif.rifasims.exception.InvalidCredentialsException;
import io.github.irfnhanif.rifasims.repository.UserRepository;
import io.github.irfnhanif.rifasims.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public User register(RegisterRequest registerRequest) {
        User newUser = new User();
        newUser.setUsername(registerRequest.getUsername());
        newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        newUser.setBranch(registerRequest.getBranch());
        newUser.setRole(UserRole.EMPLOYEE);
        newUser.setStatus(UserStatus.PENDING);
        return userRepository.save(newUser);
    }

    public String login(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new InvalidCredentialsException("User not found"));
            if (user.getStatus() == UserStatus.PENDING) {
                throw new InvalidCredentialsException("Wait for owner approval");
            }
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return jwtUtil.generateToken(userDetails);
        } catch (BadCredentialsException e) {
            // Only handle credential-related exceptions
            throw new BadRequestException("Invalid username or password");
        }
        // Let other exceptions propagate to get better error information
    }
}