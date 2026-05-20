package com.example.seclab.common;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Basic JWT util shared by simple auth controllers.
 * The JWT-vulnerability target (vuln 5) has its own dedicated VulnerableJwtUtil / HardenedJwtUtil.
 */
@Component
public class JwtUtil {

    private final byte[] keyBytes;

    public JwtUtil(@Value("${app.jwt.secret:secret}") String secret) {
        // For demo only: pad short secrets so HS256 doesn't bomb on startup.
        byte[] raw = secret.getBytes(StandardCharsets.UTF_8);
        if (raw.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(raw, 0, padded, 0, raw.length);
            this.keyBytes = padded;
        } else {
            this.keyBytes = raw;
        }
    }

    public String issue(User u) {
        return Jwts.builder()
                .subject(u.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600_000))
                .signWith(Keys.hmacShaKeyFor(keyBytes))
                .compact();
    }
}
