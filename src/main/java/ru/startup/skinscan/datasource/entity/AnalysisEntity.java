package ru.startup.skinscan.datasource.entity;

import jakarta.persistence.*;
import lombok.Setter;
import ru.startup.skinscan.datasource.mapper.JsonAnalysisResultConverter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "analysis")
public class AnalysisEntity {

    @Id
    private UUID id;

    @Column(name = "photo_id", nullable = false)
    private UUID photoId;

//    @Type(JsonType.class)
    @Convert(converter = JsonAnalysisResultConverter.class)
    @Column(name = "result", columnDefinition = "jsonb")
    private Object result;

    private String status;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Setter
    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public AnalysisEntity(UUID id, UUID photoId, Object result, String status, LocalDateTime requestedAt, LocalDateTime completedAt) {
        this.id = id;
        this.photoId = photoId;
        this.result = result;
        this.status = status;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
    }

    public AnalysisEntity(UUID photoId, Object result, String status, LocalDateTime requestedAt, LocalDateTime completedAt) {
        this(UUID.randomUUID(), photoId, result, status, requestedAt, completedAt);
    }

    public AnalysisEntity() {
    }

    public UUID id() {
        return id;
    }

    public UUID photoId() {
        return photoId;
    }

    public Object result() {
        return result;
    }

    public String status() {
        return status;
    }

    public LocalDateTime requestedAt() {
        return requestedAt;
    }

    public LocalDateTime completedAt() {
        return completedAt;
    }
}
