package com.example.seclab.jwt;

import com.example.seclab.common.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Deliberately vulnerable: weak secret, no alg pinning, lenient parser.
 */
@Component
@Profile("vulnerable")
public class VulnerableJwtUtil {
    @Value("${app.jwt.secret}")
    private String secret;

    public String issue(User u) {
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        if (raw.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(raw, 0, padded, 0, raw.length);
            raw = padded;
        }
        return Jwts.builder()
                .subject(u.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(Keys.hmacShaKeyFor(raw))
                .compact();
    }

    /**
     * \u6545\u610f\uff1a\u4e0d\u6821\u9a8c alg\uff0c\u4f7f\u7528 unsecured \u89e3\u6790\u3002
     * \u5b9e\u9645\u751f\u4ea7\u7edd\u4e0d\u8981\u8fd9\u4e48\u5199\u3002
     */
    public String parseUsername(String token) {
        Claims claims = (Claims) Jwts.parser()
                .build()
                .parse(token)
                .getPayload();
        return claims.getSubject();
    }
}
