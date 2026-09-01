package ru.startup.skinscan.exception;

import java.util.UUID;

public class NotFindPhotoInBDException extends RuntimeException {

    private final UUID id;

    public NotFindPhotoInBDException(UUID id) {
        this.id = id;
        System.out.println("Фото " + id + " не найдено в БД");
    }

    public UUID id() {
        return id;
    }
}
