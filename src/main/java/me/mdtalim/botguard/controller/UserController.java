package me.mdtalim.botguard.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.mdtalim.botguard.dto.request.CreateUserRequest;
import me.mdtalim.botguard.entity.User;
import me.mdtalim.botguard.repository.UserRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest req) {
        User user = User.builder().username(req.getUsername()).isPremium(req.isPremium()).build();
        return ResponseEntity.status(HttpStatus.CREATED).body(userRepository.save(user));
    }

    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userRepository.findAll());
    }
}
