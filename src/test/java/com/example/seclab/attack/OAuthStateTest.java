package com.example.seclab.attack;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("hardened")
class OAuthStateTest {
    @LocalServerPort int port;

    @Test
    void callbackWithoutStateRejected() {
        RestAssured.given().port(port)
                .when().get("/oauth/callback?code=any")
                .then().statusCode(400);
    }

    @Test
    void callbackWithWrongStateRejected() {
        RestAssured.given().port(port)
                .when().get("/oauth/callback?code=any&state=attacker-supplied")
                .then().statusCode(400);
    }
}
