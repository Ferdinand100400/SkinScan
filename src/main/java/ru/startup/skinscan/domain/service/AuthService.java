package ru.startup.skinscan.domain.service;

import org.springframework.stereotype.Service;
import ru.startup.skinscan.domain.model.User;
import ru.startup.skinscan.domain.model.jwt.JwtTokens;
import ru.startup.skinscan.security.jwt.JwtAuthentication;

import java.util.UUID;

@Service
public interface AuthService {
    UUID register(User user);
    JwtTokens validate(User userRequest);
    JwtTokens accessToken(String refreshToken);
    JwtTokens refreshToken(String refreshToken);
    JwtAuthentication getAuthentication();
}
