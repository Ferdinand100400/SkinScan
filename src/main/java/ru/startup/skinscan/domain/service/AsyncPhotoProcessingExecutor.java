package ru.startup.skinscan.domain.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.startup.skinscan.ML.MLClient;
import ru.startup.skinscan.ML.MLRequest;
import ru.startup.skinscan.ML.MLResponse;
import ru.startup.skinscan.datasource.mapper.AnalysisMapper;
import ru.startup.skinscan.datasource.mapper.PhotoMapper;
import ru.startup.skinscan.datasource.repository.JpaAnalysisRepository;
import ru.startup.skinscan.datasource.repository.JpaPhotoRepository;
import ru.startup.skinscan.domain.model.Analysis;
import ru.startup.skinscan.domain.model.Photo;
import ru.startup.skinscan.domain.model.StatusAnalysis;
import ru.startup.skinscan.domain.model.StatusPhoto;
import ru.startup.skinscan.exception.MLServiceException;
import ru.startup.skinscan.exception.NotFindPhotoInBDException;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.processing.mode", havingValue = "async", matchIfMissing = true)
public class AsyncPhotoProcessingExecutor implements PhotoProcessingExecutor {

    private static final Logger log = LoggerFactory.getLogger(AsyncPhotoProcessingExecutor.class);

    private final JpaPhotoRepository photoRepo;
    private final JpaAnalysisRepository analysisRepo;
    private final FileStorageService storageService;
    private final MLClient mlClient;

    public AsyncPhotoProcessingExecutor(JpaPhotoRepository photoRepo, JpaAnalysisRepository analysisRepo, FileStorageService storageService, MLClient mlClient) {
        this.photoRepo = photoRepo;
        this.analysisRepo = analysisRepo;
        this.storageService = storageService;
        this.mlClient = mlClient;
    }

    // Асинхронно обрабатывает фото
    @Async("photoTaskExecutor")
    @Override
//    @Transactional
    public void processPhoto(UUID photoId, UUID userId) {
        log.info("Запущен асинхронный процесс с фото: {}, пользователя: {}", photoId, userId);
        Photo photo = null;
        Analysis analysis = null;
        try {
            // Получаем фото из БД
            Thread.sleep(1000);
            photo = photoRepo.findById(photoId)
                    .map(PhotoMapper::entityToDto)
                    .orElseThrow(() -> new NotFindPhotoInBDException(photoId));
            // Проверяем статус (защита от двойной обработки)
            if (photo.status() != StatusPhoto.UPLOADED) {
                log.warn("Фото {} уже было обработано, статус: {}",
                        photoId, photo.status().status());
                return;
            }
            // Обновляем статус на PROCESSING
            photo.setStatus(StatusPhoto.PROCESSING);
            photoRepo.updateStatus(photoId, photo.status().status(), OffsetDateTime.now());
            log.info("Сменен статус фото {} на PROCESSING", photoId);

            // Создаем запись анализа
            analysis = new Analysis(
                    photoId,
                    null,
                    StatusAnalysis.IN_PROGRESS,
                    OffsetDateTime.now(),
                    OffsetDateTime.now()
            );
            UUID analysisId = analysisRepo.save(AnalysisMapper.dtoToEntity(analysis)).id();

            // Получаем файл из хранилища
            Optional<InputStream> fileStream = storageService.get(photo.storageFilePath());
            if (fileStream.isEmpty()) {
                throw new RuntimeException("Файл не найден в хранилище: " + photo.storageFilePath());
            }
            // Читаем файл в байтовый массив
            byte[] imageData = fileStream.get().readAllBytes();

            // Формируем запрос к ML
            MLRequest mlRequest = new MLRequest(imageData);
            // Добавляем метаданные
            MLRequest.Metadata metadata = new MLRequest.Metadata();
            metadata.setPhotoId(String.valueOf(photoId));
            metadata.setUserId(String.valueOf(userId));
            metadata.setFilename(photo.fileName());
            metadata.setMimeType(photo.mimeType());
            metadata.setFileSize(photo.fileSizeBytes());
            mlRequest.setMetadata(metadata);

            log.info("Сформирован запрос к ML сервису для фото: {}, размером: {} байт",
                    photoId, imageData.length);

            // Вызываем ML-сервис для анализа
            log.info("Вызов ML анализа фото: {}", photoId);
            long startTime = System.currentTimeMillis();
            MLResponse mlResponse = mlClient.analyze(mlRequest);
            long duration = System.currentTimeMillis() - startTime;
            log.info("ML анализ фото: {}, завершен за время: {}ms", photoId, duration);

            // Проверяем успешность ответа
            if (!mlResponse.isSuccess()) {
                String errorMsg = mlResponse.error() != null ?
                        mlResponse.error().errorMessage() : "Неизвестная ошибка ML сервиса";
                throw new MLServiceException(
                        "Ошибка ML сервиса: " + errorMsg,
                        mlResponse.error() != null ?
                                mlResponse.error().errorCode() : "ML_ERROR",
                        String.valueOf(photoId)
                );
            }

            // Сохраняем результат анализа
            analysis.setStatus(StatusAnalysis.COMPLETED);
            analysis.setResult(convertObjToMap(mlResponse.result()));
            analysis.setCompletedAt(OffsetDateTime.now());
            analysisRepo.update(analysisId, analysis.result(), analysis.status().status(), analysis.completedAt());

            // Обновляем статус фото
            photo.setStatus(StatusPhoto.ANALYZED);
            photoRepo.updateStatus(photoId, photo.status().status(), OffsetDateTime.now());
            log.info("Анализ фото {} успешно завершен", photoId);

        } catch (MLServiceException e) {
            handleMLError(photo, analysis, e);
        } catch (Exception e) {
            handleGeneralError(photo, analysis, e);
        }
    }

    // Обрабатывает ошибку ML сервиса
    private void handleMLError(Photo photo, Analysis analysis, MLServiceException e) {
        if (photo == null) {
            log.error("ML ошибка: неизвестное фото {}", e.getMessage());
            return;
        }
        UUID photoId = photoRepo.findIdByNameForUserId(photo.fileName(), photo.userId()).get();
        log.error("В процессе обработки фото {} возникла ошибка ML сервиса: {}", photoId, e.getMessage(), e);

        try {
            // Обновляем анализ
            if (analysis != null) {
                analysis.setStatus(StatusAnalysis.FAILED);
                analysis.setCompletedAt(OffsetDateTime.now());
                UUID analysisId = analysisRepo.findByPhotoId(photoId).get().id();
                analysisRepo.updateStatus(analysisId, analysis.status().status(), analysis.completedAt());
            }

            // Обновляем фото
            int retryCount = photo.retryCount() + 1;
            photo.setRetryCount(retryCount);

            // Если попыток меньше 3, оставляем в UPLOADED для повторной обработки
            if (retryCount < 3) {
                photo.setStatus(StatusPhoto.UPLOADED);
                log.warn("Фото {} будет повторно обработано, кол-во попыток: {}/3", photoId, retryCount);
            } else {
                // После 3 неудачных попыток помечаем как ERROR
                photo.setStatus(StatusPhoto.ERROR);
                log.error("Фото {} не удалось проанализировать, кол-во попыток {}", photoId, retryCount);
            }
            photoRepo.updateStatus(photoId, photo.status().status(), OffsetDateTime.now());
        } catch (Exception ex) {
            log.error("Ошибка обработчика ошибок ML для фото: {}", photoId, ex);
        }
    }

    // Обрабатывает общую ошибку
    private void handleGeneralError(Photo photo, Analysis analysis, Exception e) {
        if (photo == null) {
            log.error("Неизвестная ошибка и фото: ", e);
            return;
        }
        UUID photoId = photoRepo.findIdByNameForUserId(photo.fileName(), photo.userId()).get();
        log.error("В процессе обработки фото {} возникла неизвестная ошибка: ", photoId, e);
        try {
            // Помечаем фото как ERROR
            photo.setStatus(StatusPhoto.ERROR);
            photoRepo.updateStatus(photoId, photo.status().status(), OffsetDateTime.now());
            // Обновляем анализ
            if (analysis != null) {
                analysis.setStatus(StatusAnalysis.FAILED);
                analysis.setCompletedAt(OffsetDateTime.now());
                UUID analysisId = analysisRepo.findByPhotoId(photoId).get().id();
                analysisRepo.updateStatus(analysisId, analysis.status().status(), analysis.completedAt());
            }

        } catch (Exception ex) {
            log.error("Ошибка обработчика общих ошибок для фото: {}", photoId, ex);
        }
    }

    // Конвертирует из объекта (Result) в Map для последующего формирования Json
    private static Map<String, Object> convertObjToMap(Object obj) {
        Map<String, Object> map = new LinkedHashMap<>();
        try {
            for (Field field : obj.getClass().getDeclaredFields()) {
                JsonProperty annotation = field.getAnnotation(JsonProperty.class);
                String jsonName = (annotation != null && !annotation.value().isEmpty())
                        ? annotation.value()
                        : field.getName();
                field.setAccessible(true);
                map.put(jsonName, field.get(obj));
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Ошибка получения значений полей класса Result", e);
        }
        return map;
    }
}
