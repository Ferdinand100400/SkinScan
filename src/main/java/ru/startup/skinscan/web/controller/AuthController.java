package ru.startup.skinscan.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.startup.skinscan.web.model.UserRequest;
import ru.startup.skinscan.web.model.jwt.JwtRefreshRequest;
import ru.startup.skinscan.web.model.jwt.JwtRequest;

import java.util.UUID;

@RestController
@RequestMapping("/skinScan")
public interface AuthController {

    @PostMapping("/register")
    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Добавляет нового пользователя в базу по обязательным полям логин и пароль, необязательными являются email и телефон"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации — не заданы логин, пароль или неверный email"),
            @ApiResponse(responseCode = "409", description = "Такой пользователь уже существует"),
    })
    ResponseEntity<?> register(@RequestBody @Valid UserRequest userRequest);


    @GetMapping("/login")
    @Operation(
            summary = "Аутентификация пользователя",
            description = "Проверяет в базе пользователя по введенному логину и паролю"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован"),
            @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
    })
    ResponseEntity<?> login(@Valid @RequestBody JwtRequest request);


    @PostMapping("/refresh-token/access")
    @Operation(
            summary = "Обновление access токена",
            description = "Обновляет access токен по refresh токену (refresh токен обновляется тоже)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Токен успешно обновлен"),
            @ApiResponse(responseCode = "401", description = "Неверный или истекший refresh токен. Пользователь не существует или удален.")
    })
    ResponseEntity<?> refreshAccess(@RequestBody @Valid JwtRefreshRequest request);


    @PostMapping("/refresh-token/refresh")
    @Operation(
            summary = "Обновление refresh токена",
            description = "Обновляет refresh и access токены по refresh токену"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Токены успешно обновлены"),
            @ApiResponse(responseCode = "401", description = "Неверный или истекший refresh токен. Пользователь не существует или удален.")
    })
    ResponseEntity<?> refreshRefresh(@RequestBody @Valid JwtRefreshRequest request);

    @GetMapping("/me")
    @Operation(
            summary = "Информация о пользователе",
            description = "Получает основную информацию о пользователе"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Токен успешно обновлен"),
            @ApiResponse(responseCode = "401", description = "Неверный или истекший refresh токен"),
            @ApiResponse(responseCode = "404", description = "Пользователь не найден")
    })
    ResponseEntity<?> me(@AuthenticationPrincipal UUID userId);
}
