package ru.startup.skinscan.domain.model;

public enum StatusAnalysis {
    PENDING("Ожидание обработки"),
    IN_PROGRESS("Обработка фото"),
    COMPLETED("Анализ завершен"),
    FAILED("Ошибка обработки фото");

    private final String status;

    StatusAnalysis(String status) {
        this.status = status;
    }

    public String status() {
        return status;
    }

    public static StatusAnalysis fromStatus(String status) {
        for (StatusAnalysis value : values()) {
            if (value.status.equals(status)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Неизвестный статус: " + status);
    }
}
