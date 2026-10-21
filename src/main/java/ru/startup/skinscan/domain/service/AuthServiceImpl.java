package ru.startup.skinscan.domain.service;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.startup.skinscan.domain.model.User;
import ru.startup.skinscan.domain.model.jwt.JwtTokens;
import ru.startup.skinscan.exception.IncorrectPasswordException;
import ru.startup.skinscan.exception.InvalidTokenException;
import ru.startup.skinscan.exception.NotFindUserException;
import ru.startup.skinscan.security.jwt.JwtAuthentication;
import ru.startup.skinscan.security.jwt.JwtProvider;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final UserService userService;
    private final JwtProvider jwtProvider;

    public AuthServiceImpl(UserService userService, JwtProvider jwtProvider) {
        this.userService = userService;
        this.jwtProvider = jwtProvider;
    }

    @Override
    public UUID register(User user) {
        return userService.createUser(user);
    }

    @Override
    public JwtTokens validate(User userRequest) {
        try {
            User user = userService.findUserByLogin(userRequest.login());
            log.info("Пользователь {} найден", userRequest.login());
            if (!userService.passwordEncoder().matches(userRequest.password(), user.password())) {
                log.warn("Неверный пароль для логина '{}'", userRequest.login());
                throw new IncorrectPasswordException();
            }
            UUID userId = userService.findUserIdByLogin(userRequest.login());
            String accessToken = jwtProvider.generateAccessToken(user, userId);
            String refreshToken = jwtProvider.generateRefreshToken(userId);
            log.info("Успешный логин для пользователя с UUID '{}'", userId);
            return new JwtTokens(accessToken, refreshToken);
        } catch (NotFindUserException ignored) {
            log.warn("Пользователя с логином '{}' не существует", userRequest.login());
            throw new NotFindUserException(userRequest.login());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public JwtTokens accessToken(String refreshToken) {
        return refreshTokens(refreshToken, "access");
    }

    @Override
    @Transactional(readOnly = true)
    public JwtTokens refreshToken(String refreshToken) {
        return refreshTokens(refreshToken, "refresh");
    }

    @Override
    public JwtAuthentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthentication jwtAuthentication) {
            return jwtAuthentication;
        }
        log.debug("Текущая аутентификация не является JwtAuthentication: {}",
                authentication != null ? authentication.getClass().getSimpleName() : "null");
        return null;
    }

    private JwtTokens refreshTokens(String refreshToken, String operationType) {
        if (!jwtProvider.validateRefreshToken(refreshToken)) {
            log.warn("Обновление {}: refresh-токен невалиден", operationType);
            throw new InvalidTokenException("Refresh-токен невалиден");
        }
        Claims claims = jwtProvider.getClaims(refreshToken);
        UUID userId = jwtProvider.getUserId(claims);
        User user = userService.findUserById(userId);

        String newAccessToken = jwtProvider.generateAccessToken(user, userId);
        String newRefreshToken = jwtProvider.generateRefreshToken(userId);
        log.debug("Обновление {} для пользователя с UUID '{}'", operationType, userId);
        return new JwtTokens(newAccessToken, newRefreshToken);
    }

//    private String[] decoding(String base64) {
//        byte[] decodedBytes = Base64.getDecoder().decode(base64);
//        String credentials = new String(decodedBytes, StandardCharsets.UTF_8);
//        return credentials.split(":", 2);
//    }
}
