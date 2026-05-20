package com.example.seclab.attack;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("hardened")
class MassRegisterTest {
    @LocalServerPort int port;

    @Test
    void shouldRateLimitMassRegister() {
        int success = 0, blocked = 0;
        for (int i = 0; i < 50; i++) {
            int code = RestAssured.given().port(port)
                    .contentType(ContentType.JSON)
                    .header("X-Captcha-Token", "test-bypass")
                    .body(Map.of("username", "bot_" + i, "password", "P@ssw0rd!"))
                    .when().post("/auth/register")
                    .then().extract().statusCode();
            if (code == 200) success++;
            else if (code == 429) blocked++;
        }
        assertTrue(success <= 5, "\u6210\u529f\u6570\u5e94\u88ab\u9650\u6d41\u5230 5 \u4ee5\u5185\uff0c\u5b9e\u9645\uff1a" + success);
        assertTrue(blocked >= 40, "\u5e94\u5927\u91cf\u88ab\u9650\u6d41");
    }
}
