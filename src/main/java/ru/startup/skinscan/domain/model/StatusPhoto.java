package ru.startup.skinscan.domain.model;

public enum StatusPhoto {
    UPLOADED("Фото загружено"),
    PROCESSING("Фото в процессе анализа"),
    ANALYZED("Анализ завершен"),
    ERROR("Ошибка анализа фото");

    private final String status;

    StatusPhoto(String status) {
        this.status = status;
    }

    public String status() {
        return status;
    }

    public static StatusPhoto fromStatus(String status) {
        for (StatusPhoto value : values()) {
            if (value.status.equals(status)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус: " + status);
    }
}
