package ru.startup.skinscan.datasource.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import ru.startup.skinscan.datasource.entity.AnalysisEntity;
import ru.startup.skinscan.domain.model.Analysis;
import ru.startup.skinscan.domain.model.StatusAnalysis;
import ru.startup.skinscan.exception.MismatchBDWithDtoException;

public class AnalysisMapper {

    public static Analysis entityToDto(AnalysisEntity entity) {
        try {
            return new Analysis(
                    entity.photoId(),
                    entity.result(),
                    StatusAnalysis.fromStatus(entity.status()),
                    entity.requestedAt(),
                    entity.completedAt()
            );
        } catch (IllegalArgumentException e) {
        throw new MismatchBDWithDtoException("Статуса " + entity.status() + " не существует");
    }
    }

    public static AnalysisEntity dtoToEntity(Analysis analysis) {
        return new AnalysisEntity(
                analysis.photoId(),
                analysis.result(),
                analysis.status().status(),
                analysis.requestedAt(),
                analysis.completedAt()
        );
    }
}
