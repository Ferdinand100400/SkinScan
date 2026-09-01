package ru.startup.skinscan.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.startup.skinscan.domain.service.PhotoService;
import ru.startup.skinscan.exception.*;
import ru.startup.skinscan.web.mapper.PhotoMapperWeb;
import ru.startup.skinscan.web.model.PhotoRequest;
import ru.startup.skinscan.web.model.PhotoResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Slf4j  // для логов и внедрения фильтра логов
public class PhotoControllerImpl implements PhotoController {

    private final PhotoService photoService;

    public PhotoControllerImpl(PhotoService photoService) {
        this.photoService = photoService;
    }

    @Override
    public ResponseEntity<?> uploadPhoto(PhotoRequest photoRequest, UUID userId) {
        try {
            UUID photoId = photoService.upload(photoRequest.file(), userId);
            return ResponseEntity
                    .status(HttpStatus.ACCEPTED)
                    .body(photoId);
        } catch (PhotoWithNameAlreadyExistsException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(e.getMessage());
        } catch (LimitPhotoSizeException | UnknownMimeTypePhotoException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (StorageException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(e.message() + " " + e.path());
        }
    }

    @Override
    public ResponseEntity<?> getAllPhotos(UUID userId) {
        List<PhotoResponse> photosResponse = new ArrayList<>();
        for (Map.Entry<UUID, String> photoUrl : photoService.getAllPhotoAccessUrl(userId).entrySet()) {
            photosResponse.add(PhotoMapperWeb.dtoToResponse(
                    photoService.getMetaDataPhoto(photoUrl.getKey(), userId),
                    photoUrl.getKey(),
                    photoUrl.getValue(),
                    -1
            ));
        }
        return ResponseEntity.ok(photosResponse);
    }


    @Override
    public ResponseEntity<?> getPhotosById(UUID photoId, UUID userId) {
        try {
            String url = photoService.getPhotoAccessUrl(photoId, userId);
            PhotoResponse response = PhotoMapperWeb.dtoToResponse(photoService.getMetaDataPhoto(photoId, userId), photoId, url, -1);
            return ResponseEntity.ok(response);
        } catch (NotFindPhotoInBDException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Фото с id " + photoId + " не найдено");
        } catch (PhotoAnalysisProcessException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body("Фото находится в обработке, подождите");
        } catch (StatusPhotoIsUploadOrErrorException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY)
                    .body("Ошибка анализа фото, попробуйте загрузить снова");
        } catch (NotAccessToPhotoForUserException e) {
            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Нет доступа к фото для пользователя " + e.userId().toString());
        }
    }

    @Override
    public ResponseEntity<?> getPhotosByName(String nameFile, UUID userId) {
        try {
            UUID photoId = photoService.getPhotoIdByNameFile(nameFile, userId);
            return getPhotosById(photoId, userId);
        } catch (NotFindPhotoInBDException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Фото с наименованием " + nameFile + " не найдено");
        }
    }
}
