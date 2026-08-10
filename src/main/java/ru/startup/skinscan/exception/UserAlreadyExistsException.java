package ru.startup.skinscan.exception;

import java.util.UUID;

public class UserAlreadyExistsException extends RuntimeException {

    private final String login;

    public UserAlreadyExistsException(UUID id, String login) {
        this.login = login;
        System.out.println("Пользователь " + login + " уже существует!");
    }

    public String login() {
        return login;
    }
}
