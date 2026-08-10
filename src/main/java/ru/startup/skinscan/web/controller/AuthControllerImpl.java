package ru.startup.skinscan.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.startup.skinscan.domain.service.AuthService;
import ru.startup.skinscan.exception.IncorrectPasswordException;
import ru.startup.skinscan.exception.NotFindUserException;
import ru.startup.skinscan.exception.UserAlreadyExistsException;
import ru.startup.skinscan.web.mapper.UserMapperWeb;
import ru.startup.skinscan.web.model.UserRequest;

import java.util.UUID;

@RestController
@RequestMapping("/skinScan")
@Slf4j  // для логов и внедрения фильтра логов
public class AuthControllerImpl implements AuthController {

    private final AuthService authService;

    public AuthControllerImpl(AuthService authService) {
        this.authService = authService;
    }

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

    public ResponseEntity<?> login(String auth) {
        try {
            UUID id = authService.validate(auth);
            return ResponseEntity.ok(id);
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
}
