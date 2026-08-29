package ru.startup.skinscan.ML;

import ru.startup.skinscan.exception.MLServiceException;

public interface MLClient {

    MLResponse analyze(MLRequest request) throws MLServiceException;
    // Доступность ML сервиса
    boolean isAvailable();
}
