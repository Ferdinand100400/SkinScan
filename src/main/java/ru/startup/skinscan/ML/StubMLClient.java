package ru.startup.skinscan.ML;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.startup.skinscan.exception.MLServiceException;

import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class StubMLClient implements MLClient {

    private static final Logger log = LoggerFactory.getLogger(StubMLClient.class);

    private final Random random = new Random();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Метрики заглушки
    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong successfulRequests = new AtomicLong(0);
    private final AtomicLong failedRequests = new AtomicLong(0);
    private final Map<String, Long> averageProcessingTimes = new ConcurrentHashMap<>();

    @Value("${app.ml.client.stub.delay-min:3000}")
    private int delayMinMs;

    @Value("${app.ml.client.stub.delay-max:5000}")
    private int delayMaxMs;

    @Value("${app.ml.client.stub.failure-probability:0.05}")
    private double failureProbability;

    @Value("${app.ml.client.stub.simulate-timeout:false}")
    private boolean simulateTimeout;

    @Value("${app.ml.client.stub.timeout-delay-ms:30000}")
    private int timeoutDelayMs;

    @Override
    public MLResponse analyze(MLRequest request) throws MLServiceException {
        totalRequests.incrementAndGet();
        long startTime = System.currentTimeMillis();
        try {
            log.info("Заглушка ML: Анализ фото: {}",
                    request.metadata() != null ? request.metadata().photoId() : "unknown");

            // Имитация задержки
            int delay = simulateTimeout ? timeoutDelayMs : getRandomDelay();
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MLServiceException("ML processing interrupted", "ML_INTERRUPTED",
                        request.metadata() != null ? request.metadata().photoId() : null);
            }

            // Имитация ошибки с вероятностью failureProbability
            if (random.nextDouble() < failureProbability) {
                failedRequests.incrementAndGet();
                log.warn("Заглушка ML: Симуляция запроса");
                throw new MLServiceException(
                        "Simulated request ML service error",
                        "ML_SIMULATED_ERROR",
                        request.metadata() != null ? request.metadata().photoId() : null
                );
            }

            // 3. Генерация результата
            MLResponse response = generateResponse(request);

            long processingTime = System.currentTimeMillis() - startTime;
            successfulRequests.incrementAndGet();

            // Сохраняем метрики
            String photoId = request.metadata() != null ?
                    request.metadata().photoId() : "unknown";
            averageProcessingTimes.put(photoId, processingTime);
            log.info("Заглушка ML: Фото: {} обработано за {} ms", photoId, processingTime);
            return response;

        } catch (MLServiceException e) {
            failedRequests.incrementAndGet();
            throw e;
        } catch (Exception e) {
            failedRequests.incrementAndGet();
            throw new MLServiceException(
                    "Unexpected error in ML stub",
                    "ML_STUB_ERROR",
                    request.metadata() != null ? request.metadata().photoId() : null,
                    e
            );
        }
    }

    // Генерирует ответ в зависимости от входных данных.
    private MLResponse generateResponse(MLRequest request) {
        String photoId = request.metadata() != null ?
                request.metadata().photoId() : "unknown";

        // Разные сценарии
        int scenario = Math.abs(photoId.hashCode() % 5);
        MLResponse.Result result = new MLResponse.Result();

        switch (scenario) {
            case 0:
                // Низкий риск
                result.setRiskPercentage(randomBetween(10.0, 25.0));
                result.setCondition("benign");
                result.setFeatures(Arrays.asList("symmetry_present", "border_regular"));
                result.setRecommendation("No action needed. Regular check-up in 6 months.");
                break;

            case 1:
                // Средний риск
                result.setRiskPercentage(randomBetween(40.0, 60.0));
                result.setCondition("likely_benign");
                result.setFeatures(Arrays.asList("slight_asymmetry", "border_irregular"));
                result.setRecommendation("Consult a dermatologist within 6 months.");
                break;

            case 2:
                // Высокий риск
                result.setRiskPercentage(randomBetween(70.0, 88.0));
                result.setCondition("likely_malignant");
                result.setFeatures(Arrays.asList("asymmetry_present", "border_irregular",
                        "color_variation", "diameter_large"));
                result.setRecommendation("Consult a dermatologist within 2 weeks.");
                break;

            case 3:
                // Критический риск
                result.setRiskPercentage(randomBetween(90.0, 99.5));
                result.setCondition("malignant");
                result.setFeatures(Arrays.asList("asymmetry_severe", "border_irregular",
                        "color_variation", "diameter_large", "ulceration"));
                result.setRecommendation("IMMEDIATE consultation with a dermatologist.");
                break;

            case 4:
            default:
                // Неопределенный результат
                result.setRiskPercentage(randomBetween(20.0, 45.0));
                result.setCondition("uncertain");
                result.setFeatures(Arrays.asList("quality_insufficient", "artifact_present"));
                result.setRecommendation("Please upload a higher quality image.");
                break;
        }

        // Создаем ответ
        long processingTime = randomBetween(3000, 5000);
        return MLResponse.success(result, processingTime);
    }

    private double randomBetween(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    private int randomBetween(int min, int max) {
        return min + random.nextInt(max - min + 1);
    }

    private int getRandomDelay() {
        if (delayMinMs == delayMaxMs) {
            return delayMinMs;
        }
        return randomBetween(delayMinMs, delayMaxMs);
    }

    @Override
    public boolean isAvailable() {
        return !simulateTimeout && random.nextDouble() < 0.95;
    }

}
