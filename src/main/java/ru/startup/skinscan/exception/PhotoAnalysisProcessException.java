package ru.startup.skinscan.exception;

import java.util.UUID;

public class PhotoAnalysisProcessException extends RuntimeException {

    private final UUID id;

    public PhotoAnalysisProcessException(UUID id) {
        this.id = id;
        System.out.println("Фото " + id + " в процессе анализа");
    }

    public UUID id() {
        return id;
    }
}
