package ru.startup.skinscan.datasource.mapper;

import ru.startup.skinscan.datasource.entity.PhotoEntity;
import ru.startup.skinscan.domain.model.Photo;
import ru.startup.skinscan.domain.model.StatusPhoto;
import ru.startup.skinscan.exception.MismatchBDWithDtoException;

import java.time.LocalDateTime;
import java.util.List;

public class PhotoMapper {

    public static PhotoEntity dtoToEntity(Photo photo) {
        return new PhotoEntity(
                photo.userId(),
                photo.fileName(),
                photo.storageFilePath(),
                photo.fileSizeBytes(),
                photo.mimeType(),
                photo.status().status()
        );
    }

    public static PhotoEntity dtoToEntity(Photo photo, LocalDateTime updatedAt) {
        PhotoEntity entity = dtoToEntity(photo);
        entity.setUpdatedAt(updatedAt);
        return entity;
    }

    public static Photo entityToDto(PhotoEntity photoEntity) {
        try {
            return new Photo(
                    photoEntity.userId(),
                    photoEntity.fileName(),
                    photoEntity.storageFilePath(),
                    photoEntity.fileSizeBytes(),
                    photoEntity.mimeType(),
                    StatusPhoto.fromStatus(photoEntity.status())
            );
        } catch (IllegalArgumentException e) {
            throw new MismatchBDWithDtoException("Статуса " + photoEntity.status() + " не существует");
        }
    }

    public static List<Photo> entityListToDtoList(List<PhotoEntity> photosEntity) {
        return photosEntity.stream()
                .map(PhotoMapper::entityToDto)
                .toList();
    }
}
