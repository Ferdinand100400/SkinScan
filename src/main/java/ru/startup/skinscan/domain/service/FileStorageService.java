package ru.startup.skinscan.domain.service;

import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;

public interface FileStorageService {

    String save(InputStream inputStream, String path);
    Optional<InputStream> get(String path);
    boolean delete(String path);
    boolean exists(String path);
    String getUrl(String path);
    String getSignedUrl(String path, Duration timeout);
    long getSize(String path);
    String getMimeType(String path);
}
