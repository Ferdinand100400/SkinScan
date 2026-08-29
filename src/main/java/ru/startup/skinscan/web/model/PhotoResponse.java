package ru.startup.skinscan.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class PhotoResponse {

    private final UUID id;
    private final String status;
    private final String links;

    @JsonProperty("size_file")
    private final Long sizeFile;

    @JsonProperty("processing_time")
    private final Integer processingTimeInSeconds;

    public PhotoResponse(UUID id, String status, String links, Long sizeFile, Integer processingTimeInSeconds) {
        this.id = id;
        this.status = status;
        this.links = links;
        this.sizeFile = sizeFile;
        this.processingTimeInSeconds = processingTimeInSeconds;
    }

    public UUID id() {
        return id;
    }

    public String status() {
        return status;
    }

    public String links() {
        return links;
    }

    public Long sizeFile() {
        return sizeFile;
    }

    public Integer processingTimeInSeconds() {
        return processingTimeInSeconds;
    }
}
