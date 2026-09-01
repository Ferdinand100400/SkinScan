package ru.startup.skinscan.exception;

public class LimitPhotoSizeException extends RuntimeException {

    private final String message;

    public LimitPhotoSizeException(Long curSize, Long maxSize) {
        this.message = "Размер фото " + curSize + " байт, больше максимального значения " + maxSize + " байт";
        System.out.println(message);
    }

    @Override
    public String getMessage() {
        return message;
    }
}
