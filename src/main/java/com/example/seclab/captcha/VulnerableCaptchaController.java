package com.example.seclab.captcha;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/captcha")
@Profile("vulnerable")
public class VulnerableCaptchaController {
    // \u5168\u5c40\u5171\u4eab\u3001\u6c38\u4e45\u6709\u6548\u3001\u53ef\u91cd\u653e
    private static final Map<String, String> ANSWERS = new ConcurrentHashMap<>();

    @GetMapping("/issue")
    public Map<String, String> issue() {
        String token = UUID.randomUUID().toString();
        String answer = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        ANSWERS.put(token, answer);
        return Map.of("token", token, "image", "<svg>" + answer + "</svg>");
    }

    @PostMapping("/verify")
    public Map<String, Boolean> verify(@RequestBody Map<String, String> body) {
        boolean ok = Objects.equals(ANSWERS.get(body.get("token")), body.get("answer"));
        return Map.of("valid", ok);
    }
}
