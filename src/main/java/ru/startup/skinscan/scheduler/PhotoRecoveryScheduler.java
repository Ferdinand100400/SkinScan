package ru.startup.skinscan.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.startup.skinscan.datasource.mapper.PhotoMapper;
import ru.startup.skinscan.datasource.repository.JpaPhotoRepository;
import ru.startup.skinscan.domain.model.Photo;
import ru.startup.skinscan.domain.model.StatusPhoto;
import ru.startup.skinscan.domain.service.PhotoProcessingExecutor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Component
@EnableScheduling
public class PhotoRecoveryScheduler {

    private static final Logger log = LoggerFactory.getLogger(PhotoRecoveryScheduler.class);

    private final JpaPhotoRepository photoRepo;
    private final PhotoProcessingExecutor processingExecutor;

    // Порог в минутах: фото считается зависшим, если оно в статусе PROCESSING
    @Value("${app.recovery.stuck-threshold-minutes:5}")
    private int stuckThresholdMinutes;

    // Максимальное количество фото для восстановления за один раз.
    @Value("${app.recovery.batch-size:10}")
    private int batchSize;

    public PhotoRecoveryScheduler(JpaPhotoRepository photoRepo, PhotoProcessingExecutor processingExecutor) {
        this.photoRepo = photoRepo;
        this.processingExecutor = processingExecutor;
    }

    // Запускается каждые 2 минуты
    @Scheduled(fixedDelayString = "${app.recovery.interval-ms:120000}")
    @Transactional
    public void recoverStuckPhotos() {
        log.debug("Запуск проверки и обработки зависших фотографий");
        try {
            // Находим время, до которого фото считается зависшим
            OffsetDateTime threshold = OffsetDateTime.now().minusMinutes(stuckThresholdMinutes);

            // Ищем фото в статусе PROCESSING дольше порога
            List<Photo> stuckPhotos = PhotoMapper.entityListToDtoList(
                    photoRepo.findByStatusAndCreatedAtBefore(StatusPhoto.PROCESSING.status(), threshold)
            );

            if (stuckPhotos.isEmpty()) {
                log.debug("Зависшие фотографии не найдены");
                return;
            }

            log.info("Найдено {} зависших фотографий, запуск обработки", stuckPhotos.size());

            // Обрабатываем найденные фото
            int processedCount = 0;
            for (Photo photo : stuckPhotos) {
                if (processedCount >= batchSize) {
                    log.info("Превышен лимит обработки фото за проход: {}", batchSize);
                    break;
                }
                try {
                    recoverPhoto(photo);
                    processedCount++;
                } catch (Exception e) {
                    log.error("Ошибка обработки фото: {}", photoRepo.findIdByNameForUserId(photo.fileName(), photo.userId()), e);
                }
            }

            log.info("Обработано {} зависших фото", processedCount);
        } catch (Exception e) {
            log.error("Ошибка обработчика зависших фотографий", e);
        }
    }

     // Восстанавливает одно фото. Проверяет, не было ли фото уже обработано другим экземпляром, если нет — перезапускает обработку.
    private void recoverPhoto(Photo photo) {
        UUID photoId = photoRepo.findIdByNameForUserId(photo.fileName(), photo.userId()).get();
        log.warn("Обработка фото: {} начато", photoId);
        // Обновляем счетчик попыток
        int retryCount = photo.retryCount() + 1;
        photo.setRetryCount(retryCount);

        // Если попыток слишком много, помечаем как ERROR
        if (retryCount > 5) {
            photo.setStatus(StatusPhoto.ERROR);
            photoRepo.save(PhotoMapper.dtoToEntity(photo, OffsetDateTime.now()));
            log.error("Фото {} не удалось обработать {} раз", photoId, retryCount);
            return;
        }
        // Сбрасываем статус на UPLOADED для повторной обработки
        photo.setStatus(StatusPhoto.UPLOADED);
        photoRepo.save(PhotoMapper.dtoToEntity(photo));

        // Запускаем обработку заново
        processingExecutor.processPhoto(photoId, photo.userId());

        log.info("Фото {} обработано с: {}/5 раза", photoId, retryCount);
    }
}
