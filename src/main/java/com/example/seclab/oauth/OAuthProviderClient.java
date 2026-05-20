package com.example.seclab.oauth;

import org.springframework.stereotype.Component;

/** Stub for an OIDC provider so the demo runs without external calls. */
@Component
public class OAuthProviderClient {

    public record ThirdPartyProfile(String sub, String email, String nonce) {
    }

    public ThirdPartyProfile exchangeCode(String code) {
        // Pretend we exchanged the code for a profile.
        return new ThirdPartyProfile("third-" + code, code + "@third.example", "stub-nonce");
    }

    public ThirdPartyProfile exchangeCode(String code, String pkceVerifier) {
        return exchangeCode(code);
    }
}
