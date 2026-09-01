package ru.startup.skinscan.domain.service;

import org.springframework.stereotype.Service;
import ru.startup.skinscan.domain.model.User;
import ru.startup.skinscan.exception.IncorrectPasswordException;
import ru.startup.skinscan.exception.NotFindUserException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserService userService;

    public AuthServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UUID register(User user) {
        return userService.createUser(user);
    }

    @Override
    public UUID validate(String base64) {
        String[] values = decoding(base64);
        String login = values[0];
        String password = values[1];
        try {
            User user = userService.findUserByLogin(login);
            System.out.println("Пользователь " + login + " найден");
            if (userService.passwordEncoder().matches(password, user.password())) return userService.findUserIdByLogin(login);
            throw new IncorrectPasswordException();
        } catch (NotFindUserException ignored) {
            throw new NotFindUserException(login);
        }
    }

    private String[] decoding(String base64) {
        byte[] decodedBytes = Base64.getDecoder().decode(base64);
        String credentials = new String(decodedBytes, StandardCharsets.UTF_8);
        return credentials.split(":", 2);
    }
}
