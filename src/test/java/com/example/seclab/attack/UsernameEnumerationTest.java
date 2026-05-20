package com.example.seclab.attack;

import com.example.seclab.common.User;
import com.example.seclab.common.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("hardened")
class UsernameEnumerationTest {
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
    void shouldNotLeakWhetherUserExists() {
        ExtractableResponse<Response> nonexistent = RestAssured.given().port(port)
                .contentType(ContentType.JSON)
                .body(Map.of("username", "nonexistent_xyz", "password", "x"))
                .when().post("/auth/login").then().extract();
        ExtractableResponse<Response> wrongPassword = RestAssured.given().port(port)
                .contentType(ContentType.JSON)
                .body(Map.of("username", "alice", "password", "wrong"))
                .when().post("/auth/login").then().extract();

        assertEquals(nonexistent.statusCode(), wrongPassword.statusCode());
        assertEquals(
                nonexistent.jsonPath().getString("error"),
                wrongPassword.jsonPath().getString("error"));
    }
}
