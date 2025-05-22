package io.github.irfnhanif.rifasims.service;

import io.github.irfnhanif.rifasims.entity.User;
import io.github.irfnhanif.rifasims.entity.UserRole;
import io.github.irfnhanif.rifasims.entity.UserStatus;
import io.github.irfnhanif.rifasims.exception.BadRequestException;
import io.github.irfnhanif.rifasims.exception.InvalidCredentialsException;
import io.github.irfnhanif.rifasims.repository.UserRepository;
import io.github.irfnhanif.rifasims.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

    public User register(User user) {
        User newUser = new User();
        newUser.setUsername(user.getUsername());
        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setBranch(user.getBranch());
        newUser.setRole(UserRole.EMPLOYEE);
        newUser.setStatus(UserStatus.PENDING);
        return userRepository.save(newUser);
    }

    public String login(String username, String password) {
        try {
            Optional<User> user = userRepository.findByUsername(username);
            if (user.get().getStatus() == UserStatus.PENDING) {
                throw new InvalidCredentialsException("Wait for owner approval");
            }
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return jwtUtil.generateToken(userDetails);
        } catch (Exception e) {
            throw new BadRequestException("Invalid username or password");
        }
    }
}
