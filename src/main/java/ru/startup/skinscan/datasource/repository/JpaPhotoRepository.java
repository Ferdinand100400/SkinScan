package ru.startup.skinscan.datasource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.startup.skinscan.datasource.entity.PhotoEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaPhotoRepository extends JpaRepository<PhotoEntity, UUID> {

    @Query("SELECT p FROM PhotoEntity p WHERE p.fileName = :nameFile")
    Optional<PhotoEntity> findByName(String nameFile);

    @Query("SELECT p FROM PhotoEntity p WHERE p.userId =:userId")
    List<PhotoEntity> findAllByUserId(UUID userId);

    @Query("SELECT p FROM PhotoEntity p WHERE p.status =:status AND p.createdAt <:threshold")
    List<PhotoEntity> findByStatusAndCreatedAtBefore(String status, LocalDateTime threshold);

    @Query("SELECT p.id FROM PhotoEntity p WHERE p.fileName =:fileName")
    Optional<UUID> findIdByName(String fileName);

    @Transactional
    @Modifying
    @Query("UPDATE PhotoEntity p SET p.status = :status, p.updatedAt = :update_at WHERE p.id = :id")
    int updateStatus(
            @Param("id") UUID photoId,
            @Param("status") String status,
            @Param("update_at") LocalDateTime updateAt
    );
}
