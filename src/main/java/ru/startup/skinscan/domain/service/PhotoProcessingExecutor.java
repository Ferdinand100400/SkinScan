package ru.startup.skinscan.domain.service;

import java.util.UUID;

public interface PhotoProcessingExecutor {

    void processPhoto(UUID photoId, UUID userId);
}
