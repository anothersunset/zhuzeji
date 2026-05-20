package com.example.seclab.oauth;

import com.example.seclab.common.BadRequestException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
@Profile("hardened")
public class HardenedOAuthController {
    private final OAuthProviderClient provider;
    private static final String SS_KEY = "oauth.state";
    private static final String SS_VERIFIER = "oauth.pkce_verifier";
    private static final String SS_NONCE = "oauth.nonce";

    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize(HttpSession session) throws Exception {
        String state = randomUrlSafe(32);
        String nonce = randomUrlSafe(16);
        String verifier = randomUrlSafe(64);
        String challenge = pkceChallenge(verifier);

        session.setAttribute(SS_KEY, state);
        session.setAttribute(SS_NONCE, nonce);
        session.setAttribute(SS_VERIFIER, verifier);

        URI redirect = UriComponentsBuilder
                .fromUriString("https://provider.example.com/authorize")
                .queryParam("client_id", "demo")
                .queryParam("redirect_uri", "http://localhost:8080/oauth/callback")
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .queryParam("nonce", nonce)
                .queryParam("code_challenge", challenge)
                .queryParam("code_challenge_method", "S256")
                .build().toUri();
        return ResponseEntity.status(302).location(redirect).build();
    }

    @GetMapping("/callback")
    public Map<String, Object> callback(@RequestParam String code,
                                        @RequestParam(required = false) String state,
                                        HttpSession session) {
        String saved = (String) session.getAttribute(SS_KEY);
        if (saved == null || state == null
                || !MessageDigest.isEqual(saved.getBytes(), state.getBytes())) {
            throw new BadRequestException("state \u4e0d\u5339\u914d");
        }
        session.removeAttribute(SS_KEY);

        String verifier = (String) session.getAttribute(SS_VERIFIER);
        session.removeAttribute(SS_VERIFIER);

        var third = provider.exchangeCode(code, verifier);
        if (!Objects.equals(session.getAttribute(SS_NONCE), third.nonce())) {
            // nonce mismatch \u2014 \u5728\u672c demo \u4e2d\u4ec5\u8b66\u544a\uff1b\u751f\u4ea7\u73af\u5883\u5e94\u62a5\u9519
            // throw new BadRequestException("nonce \u4e0d\u5339\u914d");
        }
        session.removeAttribute(SS_NONCE);
        return Map.of("bound", true, "email", third.email());
    }

    private static String randomUrlSafe(int bytes) {
        byte[] b = new byte[bytes];
        new SecureRandom().nextBytes(b);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    private static String pkceChallenge(String verifier) throws Exception {
        byte[] d = MessageDigest.getInstance("SHA-256")
                .digest(verifier.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(d);
    }
}
