package ru.startup.skinscan.domain.model;


import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

public class Analysis {

    private final UUID photoId;
    private Map<String, Object> result;
    private StatusAnalysis status;
    private final OffsetDateTime requestedAt;
    private OffsetDateTime completedAt;

    public Analysis(UUID photoId, Map<String, Object> result, StatusAnalysis status, OffsetDateTime requestedAt, OffsetDateTime completedAt) {
        this.photoId = photoId;
        this.result = result;
        this.status = status;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
    }

    public UUID photoId() {
        return photoId;
    }

    public Map<String, Object> result() {
        return result;
    }

    public StatusAnalysis status() {
        return status;
    }

    public OffsetDateTime requestedAt() {
        return requestedAt;
    }

    public OffsetDateTime completedAt() {
        return completedAt;
    }

    public void setResult(Map<String, Object> result) {
        this.result = result;
    }

    public void setStatus(StatusAnalysis status) {
        this.status = status;
    }

    public void setCompletedAt(OffsetDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
