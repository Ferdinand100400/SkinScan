package ru.startup.skinscan.security.jwt;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import ru.startup.skinscan.domain.model.role.UserRole;

import java.util.List;
import java.util.UUID;

@Component
public class JwtUtil {

    private final JwtProvider jwtProvider;

    public JwtUtil(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    public JwtAuthentication createAuthentication(Claims claims, String token) {
        UUID userId = jwtProvider.getUserId(claims);
        List<UserRole> roles = jwtProvider.getRoles(claims).stream()
                .map(UserRole::new)
                .toList();
        return new JwtAuthentication(userId, roles, token);
    }
}
