package ru.startup.skinscan.exception;

public class UnknownMimeTypePhotoException extends RuntimeException {

    private final String message;

    public UnknownMimeTypePhotoException(String mimeType) {
        this.message = "Неизвестный тип файла: " + mimeType;
        System.out.println(message);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
