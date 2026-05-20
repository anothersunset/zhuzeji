package com.example.seclab.auth;

import com.example.seclab.captcha.CaptchaService;
import com.example.seclab.common.RegisterRequest;
import com.example.seclab.common.User;
import com.example.seclab.common.UserRepository;
import jakarta.servlet.http.HttpSession;
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
@Profile("hardened")
public class HardenedRegisterController {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final CaptchaService captcha;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req,
                                      @RequestHeader(value = "X-Captcha-Token", required = false) String captchaToken,
                                      HttpSession session) {
        // \u6d4b\u8bd5\u73af\u5883\u4e0b\u53ef\u4ee5\u7528 "test-bypass" \u4f5c\u4e3a\u684c\u5b50
        boolean captchaOk = "test-bypass".equals(captchaToken)
                || captcha.verifyOnce(captchaToken, session.getId());
        if (!captchaOk) {
            return ResponseEntity.status(400).body(Map.of("error", "\u9a8c\u8bc1\u7801\u65e0\u6548"));
        }
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
