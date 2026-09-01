package ru.startup.skinscan.exception;

public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException() {
        System.out.println("Ошибка авторизации: неверный пароль");
    }
}
