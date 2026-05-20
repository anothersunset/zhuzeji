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
class SmsBombTest {
    @LocalServerPort int port;

    @Test
    void shouldBlockSamePhoneFlood() {
        String phone = "13800138001";
        int ok = 0, blocked = 0;
        for (int i = 0; i < 20; i++) {
            int code = RestAssured.given().port(port)
                    .contentType(ContentType.JSON)
                    .body(Map.of("phone", phone))
                    .when().post("/sms/send")
                    .then().extract().statusCode();
            if (code == 200) ok++;
            else if (code == 429) blocked++;
        }
        assertTrue(ok <= 5);
        assertTrue(blocked >= 15);
    }
}
