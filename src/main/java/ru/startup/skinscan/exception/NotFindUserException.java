package ru.startup.skinscan.exception;

public class NotFindUserException extends RuntimeException {
    public NotFindUserException(String login) {
        System.out.println("Пользователь " + login + " не найден!");
    }
}
