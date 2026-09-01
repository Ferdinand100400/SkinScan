package ru.startup.skinscan.web.mapper;

import ru.startup.skinscan.domain.model.Photo;
import ru.startup.skinscan.web.model.PhotoResponse;

import java.util.UUID;

public class PhotoMapperWeb {

    public static PhotoResponse dtoToResponse(Photo photo, UUID photoId, String link, Integer processingTimeInSeconds) {
        return new PhotoResponse(
                photoId,
                photo.status().status(),
                link,
                photo.fileSizeBytes(),
                processingTimeInSeconds
        );
    }

}
