package ru.startup.skinscan.datasource.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "photo")
public class PhotoEntity {

    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "storage_file_path", nullable = false)
    private String storageFilePath;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Setter
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public PhotoEntity() {
    }

    public PhotoEntity(UUID id, UUID userId, String fileName, String storageFilePath, Long fileSizeBytes, String mimeType, String status, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.fileName = fileName;
        this.storageFilePath = storageFilePath;
        this.fileSizeBytes = fileSizeBytes;
        this.mimeType = mimeType;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public PhotoEntity(UUID userId, String fileName, String storageFilePath, Long fileSizeBytes, String mimeType, String status) {
        this(UUID.randomUUID(), userId, fileName, storageFilePath, fileSizeBytes, mimeType, status, LocalDateTime.now(), LocalDateTime.now());
    }

    public UUID id() {
        return id;
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

    public String status() {
        return status;
    }

    public LocalDateTime createdAt() {
        return createdAt;
    }

    public LocalDateTime updatedAt() {
        return updatedAt;
    }
}
