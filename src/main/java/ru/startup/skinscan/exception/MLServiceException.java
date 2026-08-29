package ru.startup.skinscan.exception;

public class MLServiceException extends RuntimeException {

    private final String errorCode;
    private final String photoId;

    public MLServiceException(String message) {
        super(message);
        this.errorCode = "ERROR";
        this.photoId = null;
    }

    public MLServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "ERROR";
        this.photoId = null;
    }

    public MLServiceException(String message, String errorCode, String photoId) {
        super(message);
        this.errorCode = errorCode;
        this.photoId = photoId;
    }

    public MLServiceException(String message, String errorCode, String photoId, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.photoId = photoId;
    }

    public String errorCode() { return errorCode; }
    public String photoId() { return photoId; }
}
