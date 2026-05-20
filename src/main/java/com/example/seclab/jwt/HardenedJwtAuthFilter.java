package com.example.seclab.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Profile("hardened")
public class HardenedJwtAuthFilter extends OncePerRequestFilter {
    private final HardenedJwtUtil jwt;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String h = req.getHeader("Authorization");
        if (h != null && h.startsWith("Bearer ")) {
            try {
                String username = jwt.parseUsername(h.substring(7));
                SecurityContextHolder.getContext().setAuthentication(
                        new UsernamePasswordAuthenticationToken(username, null, List.of()));
            } catch (JwtException e) {
                res.setStatus(401);
                res.setContentType("application/json");
                res.getWriter().write("{\"error\":\"invalid token\"}");
                return;
            }
        }
        chain.doFilter(req, res);
    }
}

@RestController
@RequiredArgsConstructor
@Profile("hardened")
class MeController {
    @GetMapping("/api/me")
    public Map<String, Object> me() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return Map.of("username", auth != null ? auth.getName() : "anonymous");
    }
}
