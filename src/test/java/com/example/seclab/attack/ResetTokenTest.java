package com.example.seclab.attack;

import com.example.seclab.common.User;
import com.example.seclab.common.UserRepository;
import com.example.seclab.reset.HardenedResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("hardened")
class ResetTokenTest {
    @Autowired HardenedResetService svc;
    @Autowired UserRepository userRepo;
    @Autowired PasswordEncoder encoder;

    @BeforeEach
    void seed() {
        userRepo.findByEmail("alice@test.io").orElseGet(() ->
                userRepo.save(User.builder()
                        .username("alice")
                        .passwordHash(encoder.encode("correct-horse"))
                        .email("alice@test.io")
                        .build()));
    }

    @Test
    void tokensAreNotPredictable() {
        String t1 = svc.issue("alice@test.io");
        String t2 = svc.issue("alice@test.io");
        assertNotEquals(t1, t2);
        assertTrue(t1.length() >= 40);
    }

    @Test
    void cannotReuseToken() {
        String t = svc.issue("alice@test.io");
        assertTrue(svc.confirm(t, "NewPw1!"));
        assertFalse(svc.confirm(t, "AnotherPw1!"));
    }
}
