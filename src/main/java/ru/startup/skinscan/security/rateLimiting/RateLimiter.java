package ru.startup.skinscan.security.rateLimiting;

// Интерфейс для ограничения частоты запросов
public interface RateLimiter {

     /*
     Проверяет, разрешен ли запрос с данного ключа
     @param key уникальный идентификатор (IP, userId, комбинация)
     @param limit максимальное количество запросов
     @param windowSeconds временное окно в секундах
     @return true если запрос разрешен, false если лимит превышен
     */
    boolean isAllowed(String key, int limit, long windowSeconds);

    // Возвращает количество оставшихся запросов для ключа
    long getRemainingTokens(String key);

    //Возвращает время (в секундах), через которое лимит сбросится
    long getResetTimeSeconds(String key);

    // Сбрасывает счетчик для ключа
    void reset(String key);
}
