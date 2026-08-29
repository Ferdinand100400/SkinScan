package ru.startup.skinscan.datasource.repository;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.startup.skinscan.datasource.entity.AnalysisEntity;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaAnalysisRepository extends JpaRepository<AnalysisEntity, UUID> {

    @Query("SELECT a FROM AnalysisEntity a WHERE a.photoId = :photoId")
    Optional<AnalysisEntity> findByPhotoId(UUID photoId);

    @Transactional
    @Modifying
    @Query("UPDATE AnalysisEntity a SET a.status = :status, a.completedAt = :completed_at WHERE a.id = :id")
    int updateStatus(
            @Param("id") UUID analysisId,
            @Param("status") String status,
            @Param("completed_at") LocalDateTime updateAt
    );

    @Transactional
    @Modifying
    @Query(value = "UPDATE AnalysisEntity a SET a.result =:result, a.status = :status, a.completedAt = :completed_at WHERE a.id = :id")
    int update(
            @Param("id") UUID analysisId,
            @Param("result") Object result,
            @Param("status") String status,
            @Param("completed_at") LocalDateTime updateAt
    );

}
