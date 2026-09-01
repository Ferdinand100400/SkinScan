package ru.startup.skinscan.domain.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.startup.skinscan.datasource.mapper.PhotoMapper;
import ru.startup.skinscan.datasource.repository.JpaPhotoRepository;
import ru.startup.skinscan.domain.model.Photo;
import ru.startup.skinscan.domain.model.StatusPhoto;
import ru.startup.skinscan.exception.*;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class PhotoServiceImpl implements PhotoService {

    private static final Long MAX_BYTES = 50 * 1024 * 1024L;
    private static final Logger log = LoggerFactory.getLogger(PhotoServiceImpl.class);
    private final JpaPhotoRepository photoRepo;
    private final FileStorageService fileStorageService;
    private final PhotoProcessingExecutor processingExecutor;

    public PhotoServiceImpl(JpaPhotoRepository photoRepo, FileStorageService fileStorageService, PhotoProcessingExecutor processingExecutor) {
        this.photoRepo = photoRepo;
        this.fileStorageService = fileStorageService;
        this.processingExecutor = processingExecutor;
    }

    @Transactional
    @Override
    public UUID upload(MultipartFile file, UUID userId) {
        try {
            getPhotoIdByNameFile(file.getOriginalFilename(), userId);
            throw new PhotoWithNameAlreadyExistsException(file.getOriginalFilename());
        } catch (NotFindPhotoInBDException | NotAccessToPhotoForUserException ignored) {}
        String storagePath = generateStoragePath(userId, file.getOriginalFilename());
        try {
            String savedPath = fileStorageService.save(
                    file.getInputStream(), storagePath
            );
            log.info("Фото сохранено в хранилище");
            Photo photo = new Photo(
                    userId,
                    file.getOriginalFilename(),
                    savedPath,
                    fileStorageService.getSize(storagePath),
                    fileStorageService.getMimeType(storagePath),
                    StatusPhoto.UPLOADED
            );
            if (photo.fileSizeBytes() > MAX_BYTES) {
                fileStorageService.delete(storagePath);
                log.info("Фото удалено из хранилища из-за превышения размера");
                throw new LimitPhotoSizeException(photo.fileSizeBytes(), MAX_BYTES);
            }
            if (photo.mimeType().equals("unknown")) {
                fileStorageService.delete(storagePath);
                log.info("Фото удалено из хранилища из-за неизвестного формата файла");
                throw new UnknownMimeTypePhotoException(photo.mimeType());
            }
            UUID photoId = photoRepo.save(PhotoMapper.dtoToEntity(photo)).id();
            log.info("Метаданные фото {} сохранены в БД", photoId);

            processingExecutor.processPhoto(photoId, userId);
            log.info("Запуск асинхронной обработки фото {}", photoId);
            return photoId;
        } catch (IOException e) {
            log.error("Ошибка добавления фото для пользователя {}", userId);
            throw new RuntimeException("Ошибка добавления фото для пользователя " + userId);
        }
    }

    @Override
    public UUID getPhotoIdByNameFile(String nameFile, UUID userId) {
        UUID id = photoRepo.findByNameForUserId(nameFile, userId)
                .orElseThrow(() -> new NotFindPhotoInBDException(null))
                .id();
        log.info("Получено id {} фото с именем {}", id, nameFile);
        return id;
    }

    @Override
    public Photo getMetaDataPhoto(UUID photoId, UUID userId) {
        Photo photo = PhotoMapper.entityToDto(photoRepo.findById(photoId)
                .orElseThrow(() -> new NotFindPhotoInBDException(photoId)));
        if (!photo.userId().equals(userId))
            throw new NotAccessToPhotoForUserException(userId);
        log.info("Метаданные фото из БД {} получены", photoId);
        return photo;
    }

    @Override
    public List<Photo> getAllMetaDataPhoto(UUID userId) {
        List<Photo> photos = PhotoMapper.entityListToDtoList(photoRepo.findAllByUserId(userId));
        log.info("Получены все метаданные фоток пользователя {}", userId);
        return photos;
    }

    @Override
    public String getPhotoAccessUrl(UUID photoId, UUID userId) {
        Photo photo = getMetaDataPhoto(photoId, userId);
        if (photo.status() == StatusPhoto.PROCESSING)
            throw new PhotoAnalysisProcessException(photoId);
        if (photo.status() != StatusPhoto.ANALYZED)
            throw new StatusPhotoIsUploadOrErrorException(photoId, photo.status().status());
        String signedUrl = fileStorageService.getSignedUrl(
                photo.storageFilePath(),
                Duration.ofMinutes(5)
        );
        log.info("Сгенерирован подписанный URL для фото {}: время действия 5 минут", photoId);
        return signedUrl;
    }

    @Override
    public Map<UUID, String> getAllPhotoAccessUrl(UUID userId) {
        Map<UUID, String> photosUrl = new HashMap<>();
        for (Photo photo : getAllMetaDataPhoto(userId)) {
            if (photo.status() == StatusPhoto.ANALYZED) {
                String signedUrl = fileStorageService.getSignedUrl(
                        photo.storageFilePath(),
                        Duration.ofMinutes(5)
                );
                UUID photoId = getPhotoIdByNameFile(photo.fileName(), userId);
                photosUrl.put(photoId, signedUrl);
            }
        }
        log.info("Получены URL для всех фото пользователя {}", userId);
        return photosUrl;
    }

    private String generateStoragePath(UUID userId, String originalFilename) {
        LocalDateTime now = LocalDateTime.now();
        String extension = getFileExtension(originalFilename);
        String uuid = UUID.randomUUID().toString();
        String user = userId.toString().substring(userId.toString().length() - 5);
        return String.format(
                "%s/%d/%02d/%s.%s",
                user,
                now.getYear(),
                now.getMonthValue(),
                uuid,
                extension
        );
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            return "bin";
        }
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1 || lastDot == filename.length() - 1) {
            return "bin";
        }
        return filename.substring(lastDot + 1).toLowerCase();
    }
}
