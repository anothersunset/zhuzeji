package com.example.seclab.sms;

import com.example.seclab.common.BadRequestException;
import com.example.seclab.common.TooManyRequestsException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Profile("hardened")
public class HardenedSmsService {
    private final SmsGateway gateway;
    private final Map<String, String> codes = new ConcurrentHashMap<>();

    private final Map<String, Bucket> perPhoneMinute = new ConcurrentHashMap<>();
    private final Map<String, Bucket> perPhoneHour = new ConcurrentHashMap<>();
    private final Map<String, Bucket> perPhoneDay = new ConcurrentHashMap<>();
    private final Map<String, Bucket> perIpHour = new ConcurrentHashMap<>();

    private Bucket bucket(int cap, Duration window) {
        return Bucket.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(cap)
                        .refillIntervally(cap, window).build())
                .build();
    }

    public void send(String phone, String ip) {
        if (!isValidPhone(phone)) throw new BadRequestException("\u624b\u673a\u53f7\u65e0\u6548");

        Bucket m = perPhoneMinute.computeIfAbsent(phone, k -> bucket(1, Duration.ofSeconds(60)));
        Bucket h = perPhoneHour.computeIfAbsent(phone, k -> bucket(5, Duration.ofHours(1)));
        Bucket d = perPhoneDay.computeIfAbsent(phone, k -> bucket(10, Duration.ofDays(1)));
        Bucket ipH = perIpHour.computeIfAbsent(ip, k -> bucket(20, Duration.ofHours(1)));

        if (!m.tryConsume(1) || !h.tryConsume(1) || !d.tryConsume(1) || !ipH.tryConsume(1)) {
            throw new TooManyRequestsException("\u77ed\u4fe1\u53d1\u9001\u8fc7\u4e8e\u9891\u7e41");
        }

        String code = String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
        codes.put(phone, code);
        gateway.send(phone, "\u60a8\u7684\u9a8c\u8bc1\u7801\u662f\uff1a" + code);
    }

    private boolean isValidPhone(String p) {
        return p != null && p.matches("^1[3-9]\\d{9}$");
    }
}
