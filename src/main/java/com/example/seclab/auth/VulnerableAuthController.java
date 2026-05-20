package com.example.seclab.auth;

import com.example.seclab.common.JwtUtil;
import com.example.seclab.common.LoginRequest;
import com.example.seclab.common.User;
import com.example.seclab.common.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Profile("vulnerable")
public class VulnerableAuthController {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {
        User user = userRepo.findByUsername(req.getUsername()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(404).body(Map.of("error", "\u7528\u6237\u4e0d\u5b58\u5728"));
        }
        if (!encoder.matches(req.getPassword(), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "\u5bc6\u7801\u9519\u8bef"));
        }
        return ResponseEntity.ok(Map.of("token", jwt.issue(user)));
    }
}
