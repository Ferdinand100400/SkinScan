package ru.startup.skinscan.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.startup.skinscan.domain.service.UserService;
import ru.startup.skinscan.exception.IncorrectPasswordException;
import ru.startup.skinscan.exception.NotFindUserException;
import ru.startup.skinscan.web.mapper.UserMapperWeb;
import ru.startup.skinscan.web.model.PasswordUpdateRequest;
import ru.startup.skinscan.web.model.UserRequest;

import java.util.UUID;

@RestController
@RequestMapping("/skinScan/user")
@Slf4j  // для логов и внедрения фильтра логов
public class UserControllerImpl implements UserController {

    private final UserService userService;

    public UserControllerImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<?> update(UserRequest userRequest) {
        try {
            UUID id = userService.updateUser(UserMapperWeb.requestToDto(userRequest));
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

    @Override
    public ResponseEntity<?> updatePassword(String login, PasswordUpdateRequest passwordUpdateRequest) {
        try {
            UUID id = userService.findUserIdByLogin(login);
            userService.updatePassword(id, passwordUpdateRequest.oldPassword(), passwordUpdateRequest.newPassword());
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
