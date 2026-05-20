package com.example.seclab.captcha;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Profile("hardened")
public class CaptchaService {
    private final Map<String, Entry> store = new ConcurrentHashMap<>();
    private static final Duration TTL = Duration.ofMinutes(3);

    record Entry(String answer, String sessionId, Instant expireAt, AtomicBoolean used) {
    }

    public CaptchaIssueResponse issue(String sessionId) {
        String token = UUID.randomUUID().toString();
        String answer = generateAnswer();
        store.put(token, new Entry(answer, sessionId, Instant.now().plus(TTL), new AtomicBoolean(false)));
        return new CaptchaIssueResponse(token, "<svg>" + answer + "</svg>");
    }

    /** \u4e00\u6b21\u6027 + \u7ed1\u5b9a session + \u8fc7\u671f\u68c0\u67e5 */
    public boolean verifyOnce(String token, String sessionId) {
        if (token == null) return false;
        Entry e = store.get(token);
        if (e == null) return false;
        if (Instant.now().isAfter(e.expireAt())) {
            store.remove(token);
            return false;
        }
        if (!e.sessionId().equals(sessionId)) return false;
        if (!e.used().compareAndSet(false, true)) return false;
        store.remove(token);
        return true;
    }

    public boolean checkAnswer(String token, String userAnswer, String sessionId) {
        Entry e = store.get(token);
        return e != null
                && e.sessionId().equals(sessionId)
                && Instant.now().isBefore(e.expireAt())
                && constantTimeEquals(e.answer(), userAnswer);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(a.getBytes(), b.getBytes());
    }

    private String generateAnswer() {
        return String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
    }
}
