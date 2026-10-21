package ru.startup.skinscan.web.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.startup.skinscan.domain.service.AuthService;
import ru.startup.skinscan.domain.service.UserService;
import ru.startup.skinscan.exception.IncorrectPasswordException;
import ru.startup.skinscan.exception.InvalidTokenException;
import ru.startup.skinscan.exception.NotFindUserException;
import ru.startup.skinscan.exception.UserAlreadyExistsException;
import ru.startup.skinscan.web.mapper.AuthMapperWeb;
import ru.startup.skinscan.web.mapper.UserMapperWeb;
import ru.startup.skinscan.web.model.UserRequest;
import ru.startup.skinscan.web.model.jwt.JwtRefreshRequest;
import ru.startup.skinscan.web.model.jwt.JwtRequest;
import ru.startup.skinscan.web.model.jwt.JwtResponse;

import java.util.UUID;

@RestController
@RequestMapping("/skinScan")
@Slf4j  // для логов и внедрения фильтра логов
public class AuthControllerImpl implements AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthControllerImpl(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @Override
    public ResponseEntity<?> register(UserRequest userRequest) {
        try {
            UUID id = authService.register(UserMapperWeb.requestToDto(userRequest));
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body("Пользователь успешно зарегистрирован с id: " + id);
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Пользователь " + e.login() + " уже существует");
        }
    }

    @Override
    public ResponseEntity<?> login(JwtRequest request) {
        try {
            JwtResponse response = AuthMapperWeb.dtoToResponse(authService.validate(AuthMapperWeb.requestToDto(request)));
            return ResponseEntity.ok(response);
        } catch (NotFindUserException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Пользователь не найден");
        } catch (IncorrectPasswordException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Неверный пароль");
        }
    }

    @Override
    public ResponseEntity<?> refreshAccess(JwtRefreshRequest request) {
        try {
            JwtResponse response = AuthMapperWeb.dtoToResponse(authService.accessToken(request.refreshToken()));
            return ResponseEntity.ok(response);
        } catch (InvalidTokenException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("refresh-токен невалиден");
        } catch (NotFindUserException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("refresh-токен валиден, но пользователь удален");
        }
    }

    @Override
    public ResponseEntity<?> refreshRefresh(JwtRefreshRequest request) {
        try {
            JwtResponse response = AuthMapperWeb.dtoToResponse(authService.refreshToken(request.refreshToken()));
            return ResponseEntity.ok(response);
        } catch (InvalidTokenException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("refresh-токен невалиден");
        } catch (NotFindUserException e) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("refresh-токен валиден, но пользователь удален");
        }
    }

    @Override
    public ResponseEntity<?> me(UUID userId) {
        if (userId == null) {
            log.warn("Запрос /auth/me без аутентификации");
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .build();
        }
        try {
            return ResponseEntity.ok(UserMapperWeb.dtoToResponse(
                    userService.findUserById(userId),
                    userId,
                    userService.findCreatedAtById(userId)
            ));
        } catch (NotFindUserException e) {
            return  ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Не найден пользователь");
        }
    }
}
