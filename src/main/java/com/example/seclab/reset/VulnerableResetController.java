package com.example.seclab.reset;

import com.example.seclab.common.User;
import com.example.seclab.common.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/reset")
@RequiredArgsConstructor
@Profile("vulnerable")
public class VulnerableResetController {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final Map<String, ResetEntry> tokens = new ConcurrentHashMap<>();
    private final AtomicLong counter = new AtomicLong(1000);

    record ResetEntry(Long userId, Instant createdAt) {
    }

    @PostMapping("/request")
    public Map<String, String> request(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        Optional<User> opt = userRepo.findByEmail(email);
        if (opt.isEmpty()) return Map.of("error", "\u90ae\u7bb1\u4e0d\u5b58\u5728");
        // \u987a\u5e8f\u589e\u957f + Base36\uff0c\u770b\u8d77\u6765\u8c61\u968f\u673a\u4f46\u53ef\u9884\u6d4b
        String token = Long.toString(counter.incrementAndGet(), 36);
        tokens.put(token, new ResetEntry(opt.get().getId(), Instant.now()));
        return Map.of("resetLink", "http://localhost:8080/reset/confirm?token=" + token);
    }

    @PostMapping("/confirm")
    public Map<String, Object> confirm(@RequestParam String token,
                                       @RequestBody Map<String, String> body) {
        ResetEntry e = tokens.get(token);
        if (e == null) return Map.of("ok", false);
        userRepo.findById(e.userId()).ifPresent(u -> {
            u.setPasswordHash(encoder.encode(body.get("newPassword")));
            userRepo.save(u);
        });
        return Map.of("ok", true);
    }
}
