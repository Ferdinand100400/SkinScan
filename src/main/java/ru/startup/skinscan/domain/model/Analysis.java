package ru.startup.skinscan.domain.model;


import java.time.LocalDateTime;
import java.util.UUID;

public class Analysis {

    private final UUID photoId;
    private Object result;
    private StatusAnalysis status;
    private final LocalDateTime requestedAt;
    private LocalDateTime completedAt;

    public Analysis(UUID photoId, Object result, StatusAnalysis status, LocalDateTime requestedAt, LocalDateTime completedAt) {
        this.photoId = photoId;
        this.result = result;
        this.status = status;
        this.requestedAt = requestedAt;
        this.completedAt = completedAt;
    }

    public UUID photoId() {
        return photoId;
    }

    public Object result() {
        return result;
    }

    public StatusAnalysis status() {
        return status;
    }

    public LocalDateTime requestedAt() {
        return requestedAt;
    }

    public LocalDateTime completedAt() {
        return completedAt;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void setStatus(StatusAnalysis status) {
        this.status = status;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
