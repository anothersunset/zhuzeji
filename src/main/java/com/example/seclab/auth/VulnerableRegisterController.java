package com.example.seclab.auth;

import com.example.seclab.common.RegisterRequest;
import com.example.seclab.common.User;
import com.example.seclab.common.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Profile("vulnerable")
public class VulnerableRegisterController {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
        if (userRepo.existsByUsername(req.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "\u7528\u6237\u540d\u5df2\u5b58\u5728"));
        }
        User u = User.builder()
                .username(req.getUsername())
                .passwordHash(encoder.encode(req.getPassword()))
                .createdAt(Instant.now())
                .build();
        userRepo.save(u);
        return ResponseEntity.ok(Map.of("userId", u.getId()));
    }
}
