package ru.startup.skinscan.domain.model;

import lombok.Setter;

import java.util.UUID;

public class Photo {

    private final UUID userId;
    private final String fileName;
    private final String storageFilePath;
    private final Long fileSizeBytes;
    private final String mimeType;
    @Setter
    private StatusPhoto status;
    @Setter
    private Integer retryCount;

    public Photo(UUID userId, String fileName, String storageFilePath, Long fileSizeBytes, String mimeType, StatusPhoto status) {
        this.userId = userId;
        this.fileName = fileName;
        this.storageFilePath = storageFilePath;
        this.fileSizeBytes = fileSizeBytes;
        this.mimeType = mimeType;
        this.status = status;
        this.retryCount = 0;
    }

    public UUID userId() {
        return userId;
    }

    public String fileName() {
        return fileName;
    }

    public String storageFilePath() {
        return storageFilePath;
    }

    public Long fileSizeBytes() {
        return fileSizeBytes;
    }

    public String mimeType() {
        return mimeType;
    }

    public StatusPhoto status() {
        return status;
    }

    public Integer retryCount() {
        return retryCount;
    }
}
