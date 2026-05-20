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
@Profile("hardened")
public class HardenedAuthController {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwt;

    /** \u9884\u751f\u6210\u7684\u5047\u54c8\u5e0c\uff0c\u7528\u4e8e\u4e0d\u5b58\u5728\u7528\u6237\u7684\u6052\u65f6\u95f4\u6bd4\u5bf9 */
    private static final String DUMMY_HASH =
            "$2a$10$7EqJtq98hPqEX7fNZaFWoOa6XQGB3qfQ4S6r0kqAyT4mD3ZeYxKxC";

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest req) {
        User user = userRepo.findByUsername(req.getUsername()).orElse(null);
        String hash = (user != null) ? user.getPasswordHash() : DUMMY_HASH;
        boolean passwordMatch = encoder.matches(req.getPassword(), hash);
        if (user == null || !passwordMatch) {
            return ResponseEntity.status(401).body(Map.of("error", "\u7528\u6237\u540d\u6216\u5bc6\u7801\u9519\u8bef"));
        }
        return ResponseEntity.ok(Map.of("token", jwt.issue(user)));
    }
}
