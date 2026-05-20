package com.example.seclab.jwt;

import com.example.seclab.common.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Component
@Profile("hardened")
public class HardenedJwtUtil {
    private final SecretKey key;
    private final JwtParser parser;
    private static final String ISSUER = "seclab";
    private static final String AUDIENCE = "seclab-api";

    public HardenedJwtUtil(@Value("${app.jwt.secret}") String base64Secret) {
        byte[] raw = Decoders.BASE64.decode(base64Secret);
        if (raw.length < 32) {
            throw new IllegalStateException("JWT \u5bc6\u94a5\u5fc5\u987b \u2265 32 \u5b57\u8282");
        }
        this.key = Keys.hmacShaKeyFor(raw);
        this.parser = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(ISSUER)
                .requireAudience(AUDIENCE)
                .build();
    }

    public String issue(User u) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(ISSUER)
                .audience().add(AUDIENCE).and()
                .subject(u.getUsername())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(900)))
                .signWith(key, Jwts.SIG.HS256)
                .compact();
    }

    public String parseUsername(String token) {
        Jws<Claims> jws = parser.parseSignedClaims(token);
        String alg = jws.getHeader().getAlgorithm();
        if (!"HS256".equals(alg)) {
            throw new JwtException("\u4e0d\u53d7\u4fe1\u4efb\u7684 alg: " + alg);
        }
        return jws.getPayload().getSubject();
    }
}
