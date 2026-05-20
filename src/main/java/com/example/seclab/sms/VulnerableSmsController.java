package com.example.seclab.sms;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/sms")
@RequiredArgsConstructor
@Profile("vulnerable")
public class VulnerableSmsController {
    private final SmsGateway gateway;
    private final Map<String, String> codes = new ConcurrentHashMap<>();

    @PostMapping("/send")
    public Map<String, Object> send(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        codes.put(phone, code);
        gateway.send(phone, "\u60a8\u7684\u9a8c\u8bc1\u7801\u662f\uff1a" + code);
        return Map.of("ok", true);
    }
}
