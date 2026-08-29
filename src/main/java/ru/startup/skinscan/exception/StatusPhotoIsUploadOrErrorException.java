package ru.startup.skinscan.exception;

import java.util.UUID;

public class StatusPhotoIsUploadOrErrorException extends RuntimeException {

    private final UUID id;
    private final String status;

    public StatusPhotoIsUploadOrErrorException(UUID id, String status) {
        this.id = id;
        this.status = status;
        System.out.println("Ошибка: фото " + id + " в статусе " + status);
    }

    public UUID id() {
        return id;
    }

    public String status() {
        return status;
    }
}
