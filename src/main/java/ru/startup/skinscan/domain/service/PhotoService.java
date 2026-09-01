package ru.startup.skinscan.domain.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.startup.skinscan.domain.model.Photo;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public interface PhotoService {

    UUID upload(MultipartFile file, UUID userId);
    Photo getMetaDataPhoto(UUID photoId, UUID userId);
    List<Photo> getAllMetaDataPhoto(UUID userId);
    String getPhotoAccessUrl(UUID photoId, UUID userId);
    Map<UUID, String> getAllPhotoAccessUrl(UUID userId);
    UUID getPhotoIdByNameFile(String nameFile, UUID userId);

}
