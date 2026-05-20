package com.example.seclab.oauth;

import com.example.seclab.common.User;
import com.example.seclab.common.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/oauth")
@RequiredArgsConstructor
@Profile("vulnerable")
public class VulnerableOAuthController {
    private final UserRepository userRepo;
    private final OAuthProviderClient provider;

    @GetMapping("/authorize")
    public ResponseEntity<Void> authorize() {
        URI redirect = URI.create(
                "https://provider.example.com/authorize"
                        + "?client_id=demo&redirect_uri=http://localhost:8080/oauth/callback");
        return ResponseEntity.status(302).location(redirect).build();
    }

    @GetMapping("/callback")
    public Map<String, Object> callback(@RequestParam String code,
                                        @RequestParam(required = false) String state,
                                        HttpServletRequest req) {
        var third = provider.exchangeCode(code);
        Long localUserId = (Long) req.getSession().getAttribute("userId");
        if (localUserId != null) {
            Optional<User> opt = userRepo.findById(localUserId);
            opt.ifPresent(u -> {
                u.setEmail(third.email());
                userRepo.save(u);
            });
        }
        return Map.of("bound", true, "email", third.email());
    }
}
