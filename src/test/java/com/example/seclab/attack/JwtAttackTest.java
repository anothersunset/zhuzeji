package com.example.seclab.attack;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("hardened")
class JwtAttackTest {
    @LocalServerPort int port;

    @Test
    void rejectsAlgNone() {
        String header = b64Url("{\"alg\":\"none\",\"typ\":\"JWT\"}");
        String payload = b64Url("{\"sub\":\"admin\",\"exp\":9999999999}");
        String token = header + "." + payload + ".";
        RestAssured.given().port(port)
                .header("Authorization", "Bearer " + token)
                .when().get("/api/me")
                .then().statusCode(401);
    }

    @Test
    void rejectsWeakSecretSigned() throws Exception {
        String token = signHs256("secret", "alice");
        RestAssured.given().port(port)
                .header("Authorization", "Bearer " + token)
                .when().get("/api/me")
                .then().statusCode(401);
    }

    private String b64Url(String s) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private String signHs256(String secret, String sub) throws Exception {
        String h = b64Url("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String p = b64Url("{\"sub\":\"" + sub + "\",\"exp\":9999999999}");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] sig = mac.doFinal((h + "." + p).getBytes(StandardCharsets.UTF_8));
        return h + "." + p + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
    }
}
