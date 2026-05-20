package com.example.seclab.reset;

import com.example.seclab.common.User;
import com.example.seclab.common.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Profile("hardened")
public class HardenedResetService {
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final Map<String, ResetEntry> tokens = new ConcurrentHashMap<>();
    private static final Duration TTL = Duration.ofMinutes(15);
    private static final int TOKEN_BYTES = 32;

    record ResetEntry(String tokenHash, Long userId, Instant expireAt, AtomicBoolean used) {
    }

    public String issue(String email) {
        Optional<User> opt = userRepo.findByEmail(email);
        if (opt.isEmpty()) return null;
        byte[] raw = new byte[TOKEN_BYTES];
        new SecureRandom().nextBytes(raw);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
        String hash = sha256(token);
        tokens.put(hash, new ResetEntry(hash, opt.get().getId(),
                Instant.now().plus(TTL), new AtomicBoolean(false)));
        return token;
    }

    public boolean confirm(String token, String newPassword) {
        if (token == null) return false;
        String hash = sha256(token);
        ResetEntry e = tokens.get(hash);
        if (e == null) return false;
        if (Instant.now().isAfter(e.expireAt())) {
            tokens.remove(hash);
            return false;
        }
        if (!e.used().compareAndSet(false, true)) return false;
        tokens.remove(hash);
        userRepo.findById(e.userId()).ifPresent(u -> {
            u.setPasswordHash(encoder.encode(newPassword));
            userRepo.save(u);
        });
        return true;
    }

    private String sha256(String s) {
        try {
            byte[] d = MessageDigest.getInstance("SHA-256")
                    .digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
