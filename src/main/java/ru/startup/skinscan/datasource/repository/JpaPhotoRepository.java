package ru.startup.skinscan.datasource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.startup.skinscan.datasource.entity.PhotoEntity;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaPhotoRepository extends JpaRepository<PhotoEntity, UUID> {

    @Query("SELECT p FROM PhotoEntity p WHERE p.fileName = :nameFile AND p.userId = :userId")
    Optional<PhotoEntity> findByNameForUserId(String nameFile, UUID userId);

    @Query("SELECT p FROM PhotoEntity p WHERE p.userId =:userId")
    List<PhotoEntity> findAllByUserId(UUID userId);

    @Query("SELECT p FROM PhotoEntity p WHERE p.status =:status AND p.createdAt < CAST(:threshold AS OffsetDateTime)")
    List<PhotoEntity> findByStatusAndCreatedAtBefore(String status, OffsetDateTime threshold);

    @Query("SELECT p.id FROM PhotoEntity p WHERE p.fileName =:fileName AND p.userId = :userId")
    Optional<UUID> findIdByNameForUserId(String fileName, UUID userId);

    @Transactional
    @Modifying
    @Query("UPDATE PhotoEntity p SET p.status = :status, p.updatedAt = CAST(:update_at AS OffsetDateTime) WHERE p.id = :id")
    int updateStatus(
            @Param("id") UUID photoId,
            @Param("status") String status,
            @Param("update_at") OffsetDateTime updateAt
    );
}
