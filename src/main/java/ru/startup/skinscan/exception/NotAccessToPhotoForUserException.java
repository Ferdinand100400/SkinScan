package ru.startup.skinscan.exception;

import java.util.UUID;

public class NotAccessToPhotoForUserException extends RuntimeException {

    private final UUID userId;

    public NotAccessToPhotoForUserException(UUID userId) {
        this.userId = userId;
        System.out.println("Нет доступа к фото для пользователя " + userId);
    }

    public UUID userId() {
        return userId;
    }
}
