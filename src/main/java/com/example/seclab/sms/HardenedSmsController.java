package com.example.seclab.sms;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
@Profile("hardened")
public class HardenedSmsController {
    private final HardenedSmsService svc;

    @PostMapping("/send")
    public Map<String, Object> send(@RequestBody Map<String, String> body, HttpServletRequest req) {
        svc.send(body.get("phone"), clientIp(req));
        return Map.of("ok", true);
    }

    private String clientIp(HttpServletRequest req) {
        String xff = req.getHeader("X-Forwarded-For");
        return (xff != null && !xff.isBlank()) ? xff.split(",")[0].trim() : req.getRemoteAddr();
    }
}
