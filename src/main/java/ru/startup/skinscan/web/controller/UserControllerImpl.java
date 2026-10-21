package ru.startup.skinscan.web.controller;

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
public class UserControllerImpl implements UserController {

    private final UserService userService;

    public UserControllerImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<?> update(UserRequest userRequest, UUID loginUserId) {
        try {
            UUID id = userService.findUserIdByLogin(userRequest.login());
            if (!id.equals(loginUserId))
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Нет прав для изменения данных другого пользователя");
            userService.updateUser(UserMapperWeb.requestToDto(userRequest));
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
    public ResponseEntity<?> updatePassword(String login, PasswordUpdateRequest passwordUpdateRequest, UUID loginUserId) {
        try {
            UUID id = userService.findUserIdByLogin(login);
            if (!id.equals(loginUserId))
                return ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body("Нет прав для изменения пароля другого пользователя");
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
