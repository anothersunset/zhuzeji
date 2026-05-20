package com.example.seclab.timing;

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
@Profile("vulnerable")
public class TimingVulnerableLogin {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    /**
     * \u4f3c\u4e4e\u4fee\u590d\u4e86\u9776\u70b9 1 \uff08\u54cd\u5e94\u4f53\u4e00\u81f4\uff09\uff0c\u4f46\u4ecd\u53ef\u88ab\u65f6\u5e8f\u533a\u5206\u3002
     */
    @PostMapping("/login-v2")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        User u = userRepo.findByUsername(req.getUsername()).orElse(null);
        if (u == null) {
            return ResponseEntity.status(401).body(Map.of("error", "\u7528\u6237\u540d\u6216\u5bc6\u7801\u9519\u8bef"));
        }
        if (!encoder.matches(req.getPassword(), u.getPasswordHash())) {
            return ResponseEntity.status(401).body(Map.of("error", "\u7528\u6237\u540d\u6216\u5bc6\u7801\u9519\u8bef"));
        }
        return ResponseEntity.ok(Map.of("token", "..."));
    }
}
