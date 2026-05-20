package com.example.seclab.attack;

import com.example.seclab.common.User;
import com.example.seclab.common.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Arrays;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("hardened")
class TimingAttackTest {
    @LocalServerPort int port;
    @Autowired UserRepository userRepo;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void seed() {
        if (!userRepo.existsByUsername("alice")) {
            userRepo.save(User.builder()
                    .username("alice")
                    .passwordHash(encoder.encode("correct-horse"))
                    .build());
        }
    }

    @Test
    void timingDifferenceShouldBeSmall() {
        // \u9884\u70ed\uff0c\u907f\u514d\u9996\u8c03\u5f00\u9500\u5e72\u6270
        medianMs("warmup", 5);
        double existing = medianMs("alice", 30);
        double missing = medianMs("definitely_not_here", 30);
        double ratio = Math.max(existing, missing) / Math.min(existing, missing);
        assertTrue(ratio < 1.5,
                "\u65f6\u5e8f\u6bd4\u8fc7\u5927\uff1aexisting=" + existing + ", missing=" + missing);
    }

    private double medianMs(String username, int n) {
        double[] ts = new double[n];
        for (int i = 0; i < n; i++) {
            long t0 = System.nanoTime();
            RestAssured.given().port(port)
                    .contentType(ContentType.JSON)
                    .body(Map.of("username", username, "password", "wrong"))
                    .when().post("/auth/login-v2")
                    .then().extract();
            ts[i] = (System.nanoTime() - t0) / 1_000_000.0;
        }
        Arrays.sort(ts);
        return ts[n / 2];
    }
}
