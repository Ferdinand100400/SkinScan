package ru.startup.skinscan.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.startup.skinscan.web.model.PasswordUpdateRequest;
import ru.startup.skinscan.web.model.UserRequest;

@RestController
@RequestMapping("/skinScan/user")
public interface UserController {

    @PutMapping("/update")
    @Operation(
            summary = "Изменение данных пользователя",
            description = "Изменяет данные пользователя: email, телефон"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Данные пользователя успешно изменены"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации — не заданы логин или неверный email"),
            @ApiResponse(responseCode = "401", description = "Неверный логин")
    })
    ResponseEntity<?> update(@RequestBody @Valid UserRequest userRequest);

    @PutMapping("/update/{login}")
    @Operation(
            summary = "Изменение пароля пользователя",
            description = "Изменяет пароль пользователя"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Пароль пользователя успешно изменен"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации — пароль не соответствует формату"),
            @ApiResponse(responseCode = "401", description = "Неверный логин или старый пароль")
    })
    ResponseEntity<?> updatePassword(@PathVariable String login, @RequestBody @Valid PasswordUpdateRequest passwordUpdateRequest);
}
