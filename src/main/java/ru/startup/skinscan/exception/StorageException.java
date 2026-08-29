package ru.startup.skinscan.exception;

public class StorageException extends RuntimeException {

    private final String message;
    private final String path;

    public StorageException(String message, String path) {
        this.message = message;
        this.path = path;
    }

    public String message() {
        return message;
    }

    public String path() {
        return path;
    }
}
