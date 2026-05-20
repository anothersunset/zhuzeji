package com.example.seclab.timing;

import com.example.seclab.common.JwtUtil;
import com.example.seclab.common.LoginRequest;
import com.example.seclab.common.User;
import com.example.seclab.common.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Profile("hardened")
public class TimingHardenedLogin {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    private static final String DUMMY_HASH =
            "$2a$10$7EqJtq98hPqEX7fNZaFWoOa6XQGB3qfQ4S6r0kqAyT4mD3ZeYxKxC";

    @PostMapping("/login-v2")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User u = userRepo.findByUsername(req.getUsername()).orElse(null);
        String hash = (u != null) ? u.getPasswordHash() : DUMMY_HASH;
        boolean match = encoder.matches(req.getPassword(), hash);
        boolean ok = (u != null) && match;
        if (!ok) {
            return ResponseEntity.status(401).body(Map.of("error", "\u7528\u6237\u540d\u6216\u5bc6\u7801\u9519\u8bef"));
        }
        return ResponseEntity.ok(Map.of("token", jwt.issue(u)));
    }
}
