package ru.startup.skinscan.security.rateLimiting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

// In-Memory реализация RateLimiter с фиксированным окном.
@Service
public class RateLimiterInMemory implements RateLimiter {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterInMemory.class);

    // Хранит счетчики для каждого ключа
    // Структура: Map<key, AtomicLong> - атомарный счетчик для потокобезопасности
    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

    // Хранит время начала текущего окна для каждого ключа
    private final ConcurrentHashMap<String, Long> windowStartTimes = new ConcurrentHashMap<>();

    // Планировщик для очистки старых записей (предотвращение утечек памяти)
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    @Value("${app.rateLimiting.cleanupIntervalSeconds:300}")
    private long cleanupIntervalSeconds;

    @Value("${app.rateLimiting.maxKeys:10000}")
    private int maxKeys;

    @PostConstruct
    public void init() {
        // Запускаем периодическую очистку старых записей
        cleanupScheduler.scheduleAtFixedRate(
                this::cleanupExpiredEntries,
                cleanupIntervalSeconds,
                cleanupIntervalSeconds,
                TimeUnit.SECONDS
        );
        log.info("RateLimiter инициализирован с интервалом очистки: {}с", cleanupIntervalSeconds);
    }

    @PreDestroy
    public void destroy() {
        cleanupScheduler.shutdown();
        try {
            if (!cleanupScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupScheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            cleanupScheduler.shutdownNow();
        }
    }

    @Override
    public boolean isAllowed(String key, int limit, long windowSeconds) {
        if (key == null || key.isEmpty()) {
            log.warn("Значение параметра, ограничивающего скорость, равно null или пусто, что позволяет запросить");
            return true;
        }

        long now = Instant.now().getEpochSecond();
        long windowStart = now / windowSeconds * windowSeconds; // Округление до начала окна

        // Получаем или создаем счетчик для ключа
        AtomicLong counter = counters.computeIfAbsent(key, k -> new AtomicLong(0));

        // Получаем время начала текущего окна для ключа
        Long existingWindowStart = windowStartTimes.get(key);

        if (existingWindowStart == null || existingWindowStart != windowStart) {
            // Новое окно - сбрасываем счетчик и обновляем время
            counters.put(key, new AtomicLong(0));
            windowStartTimes.put(key, windowStart);
            counter = counters.get(key);
        }

        // Атомарное увеличение счетчика
        long count = counter.incrementAndGet();

        // Проверяем лимит
        if (count > limit) {
            log.debug("Превышен лимит по ключам: {}, кол-во: {}, лимит: {}", key, count, limit);
            return false;
        }

        // Проверяем, не превысили ли мы максимальное количество ключей (защита от DoS)
        if (counters.size() > maxKeys) {
            log.warn("Слишком много ключей, ограничивающих скорость: {}, принудительно очищаем", counters.size());
            cleanupExpiredEntries();
        }

        return true;
    }

    @Override
    public long getRemainingTokens(String key) {
        if (key == null || !counters.containsKey(key)) {
            return 0;
        }
        return counters.get(key).get();
    }

    @Override
    public long getResetTimeSeconds(String key) {
        Long windowStart = windowStartTimes.get(key);
        if (windowStart == null) {
            return 0;
        }
        // Время до конца текущего окна
        long now = Instant.now().getEpochSecond();
        return Math.max(0, windowStart + 60 - now);
    }

    @Override
    public void reset(String key) {
        counters.remove(key);
        windowStartTimes.remove(key);
        log.info("Сброс счетчика ограничения скорости для ключа: {}", key);
    }

    // Очищает записи, у которых время начала окна устарело. Защита от утечек памяти.
    private void cleanupExpiredEntries() {
        long now = Instant.now().getEpochSecond();
        int removed = 0;

        for (String key : windowStartTimes.keySet()) {
            Long windowStart = windowStartTimes.get(key);
            if (windowStart == null) {
                continue;
            }
            // Если окно закончилось более 2 минут назад - удаляем
            if (now - windowStart > 120) {
                counters.remove(key);
                windowStartTimes.remove(key);
                removed++;
            }
        }

        if (removed > 0) {
            log.debug("Очищены {} записи с истекшим сроком действия, ограничивающие скорость", removed);
        }
    }
}
