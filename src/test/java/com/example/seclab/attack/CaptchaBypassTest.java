package com.example.seclab.attack;

import com.example.seclab.captcha.CaptchaIssueResponse;
import com.example.seclab.captcha.CaptchaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("hardened")
class CaptchaBypassTest {
    @Autowired CaptchaService captcha;

    @Test
    void cannotReuseCaptcha() {
        CaptchaIssueResponse resp = captcha.issue("sess-1");
        assertTrue(captcha.verifyOnce(resp.token(), "sess-1"));
        assertFalse(captcha.verifyOnce(resp.token(), "sess-1"));
    }

    @Test
    void cannotCrossSessionVerify() {
        CaptchaIssueResponse resp = captcha.issue("sess-A");
        assertFalse(captcha.verifyOnce(resp.token(), "sess-B"));
    }
}
