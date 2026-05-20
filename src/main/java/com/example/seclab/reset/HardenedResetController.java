package com.example.seclab.reset;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/reset")
@RequiredArgsConstructor
@Profile("hardened")
public class HardenedResetController {
    private final HardenedResetService svc;

    @PostMapping("/request")
    public Map<String, String> request(@RequestBody Map<String, String> body) {
        String token = svc.issue(body.get("email"));
        // \u540c\u4e00\u8fd4\u56de\u4f53\uff0c\u9632\u90ae\u7bb1\u679a\u4e3e\uff1b\u5b9e\u9645\u751f\u4ea7\u4e2d\u90fd\u8d70\u90ae\u4ef6。\u672c demo \u53d6\u5de7\u8fd4\u56de\u4e86 token\u4f9b\u4f60\u9a8c\u8bc1
        if (token == null) return Map.of("message", "\u5982\u8be5\u90ae\u7bb1\u5b58\u5728\uff0c\u91cd\u7f6e\u94fe\u63a5\u5df2\u53d1\u9001");
        return Map.of("resetLink", "http://localhost:8080/reset/confirm?token=" + token);
    }

    @PostMapping("/confirm")
    public Map<String, Object> confirm(@RequestParam String token,
                                       @RequestBody Map<String, String> body) {
        boolean ok = svc.confirm(token, body.get("newPassword"));
        return Map.of("ok", ok);
    }
}
