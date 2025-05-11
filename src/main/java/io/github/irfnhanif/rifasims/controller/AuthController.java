package io.github.irfnhanif.rifasims.controller;

import io.github.irfnhanif.rifasims.entity.User;
import io.github.irfnhanif.rifasims.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public void register(@RequestBody User user) {

    }

    @PostMapping("/login")
    public void login(@RequestBody User user) {}

//    @PostMapping("/refresh-token")
//
}
