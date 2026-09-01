package ru.startup.skinscan.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.startup.skinscan.web.model.UserRequest;

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
    ResponseEntity<?> login(@RequestParam String auth);
}
