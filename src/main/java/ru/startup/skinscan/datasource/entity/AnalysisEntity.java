package ru.startup.skinscan.datasource.entity;

import jakarta.persistence.*;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "analysis")
public class AnalysisEntity {

    @Id
    private UUID id;

    @Column(name = "photo_id", nullable = false)
    private UUID photoId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "result", columnDefinition = "jsonb")
    private Map<String, Object> result;

    private String status;

    @Column(name = "requested_at")
    private OffsetDateTime requestedAt;

    @Setter
    @Column(name = "completed_at")
    private OffsetDateTime completedAt;

    public AnalysisEntity(UUID id, UUID photoId, Map<String, Object> result, String status, OffsetDateTime requestedAt, OffsetDateTime completedAt) {
        this.id = id;
        this.photoId = photoId;
        this.result = result;
        this.status = status;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
    }

    public AnalysisEntity(UUID photoId, Map<String, Object> result, String status, OffsetDateTime requestedAt, OffsetDateTime completedAt) {
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

    public Map<String, Object> result() {
        return result;
    }

    public String status() {
        return status;
    }

    public OffsetDateTime requestedAt() {
        return requestedAt;
    }

    public OffsetDateTime completedAt() {
        return completedAt;
    }
}
