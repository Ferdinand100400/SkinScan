package ru.startup.skinscan.domain.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import ru.startup.skinscan.exception.StorageException;
import ru.startup.skinscan.exception.UnknownMimeTypePhotoException;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;

public class MinioFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(MinioFileStorageService.class);

    private MinioClient minioClient;

    @Value("${app.storage.minio.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${app.storage.minio.access-key:minioadmin}")
    private String accessKey;

    @Value("${app.storage.minio.secret-key:minioadmin}")
    private String secretKey;

    @Value("${app.storage.minio.bucket:skinScan-photos}")
    private String bucket;

    @Value("${app.storage.minio.region:us-east-1}")
    private String region;

    @Value("${app.storage.minio.create-bucket-on-start:true}")
    private boolean createBucketOnStart;

    @Value("${app.storage.minio.bucket-policy:public-read}")
    private String bucketPolicy;

    @Value("${app.storage.minio.connection-timeout:5000}")
    private long connectionTimeout;

    @Value("${app.storage.minio.read-timeout:10000}")
    private long readTimeout;


    // Время жизни подписанных URL в секундах (по умолчанию 1 час)
    private static final int SIGNED_URL_EXPIRY_SECONDS = 3600;

    @PostConstruct
    public void init() {
        log.info("Инициализация MinIO хранилища фотографий с: {}, bucket: {}", endpoint, bucket);

        try {
            // Создаем клиент MinIO
            this.minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .region(region)
                    .build();
            // Проверяем доступность MinIO
            testConnection();
            // Создаем бакет если нужно
            if (createBucketOnStart) {
                createBucketIfNotExists();
            }
            log.info("Инициализация MinIO хранилища фотографий успешно завершена");

        } catch (Exception e) {
            throw new StorageException(
                    "Ошибка инициализации MinIO хранилища фотографий",
                    null
            );
        }
    }

    // Проверяет соединение с MinIO
    private void testConnection() {
        try {
            log.info("Тестирование соединения с MinIO: {}", endpoint);
            // Простая проверка - пытаемся выполнить операцию bucketExists
            minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucket)
                    .build());

            log.info("Тест MinIO прошел успешно");

        } catch (Exception e) {
            log.error("Ошибка соединения с MinIO: {}", e.getMessage());
            throw new StorageException(
                    "MinIO не доступно",
                    null
            );
        }
    }


    // Создает бакет если он не существует. Также настраивает политику доступа
    private void createBucketIfNotExists() {
        try {
            boolean bucketExists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );
            if (!bucketExists) {
                log.info("Создаем бакет: {}", bucket);
                // Создаем бакет
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).region(region).build()
                );
                log.info("Бакет создан: {}", bucket);
                // Настраиваем политику доступа
                configureBucketPolicy();
            } else {
                log.info("Бакет уже существует: {}", bucket);
            }
        } catch (Exception e) {
            throw new StorageException(
                    "Ошибка создания бакета",
                    bucket
            );
        }
    }

    // Настраивает политику доступа к бакету
    private void configureBucketPolicy() {
        try {
            String policyJson = buildPolicyJson();
            if (policyJson != null && !policyJson.isEmpty()) {
                minioClient.setBucketPolicy(
                        SetBucketPolicyArgs.builder()
                                .bucket(bucket)
                                .config(policyJson)
                                .build()
                );
                log.info("Политика бакета: {}", bucketPolicy);
            } else {
                log.info("Нет политики для бакета");
            }

        } catch (Exception e) {
            log.warn("Ошибка установки политики бакета: {}", e.getMessage());
        }
    }

    // Генерирует политику доступа к бакету в формате JSON
    private String buildPolicyJson() {
        if ("public-read".equals(bucketPolicy)) {
            return String.format(
                    "{\n" +
                            "  \"Version\": \"2012-10-17\",\n" +
                            "  \"Statement\": [\n" +
                            "    {\n" +
                            "      \"Effect\": \"Allow\",\n" +
                            "      \"Principal\": {\"AWS\": [\"*\"]},\n" +
                            "      \"Action\": [\"s3:GetObject\"],\n" +
                            "      \"Resource\": [\"arn:aws:s3:::%s/*\"]\n" +
                            "    }\n" +
                            "  ]\n" +
                            "}",
                    bucket
            );
        } else if ("private".equals(bucketPolicy)) {
            // Приватный бакет - только подписанные URL
            return null;
        } else {
            log.warn("Неизвестная политика бакета: {}", bucketPolicy);
            return null;
        }
    }

    @Override
    public String save(InputStream inputStream, String path) {
        validatePath(path);
        try {
            log.debug("Сохранение файла в MinIO: бакет={}, путь={}", bucket, path);
            // Сохраняем файл в MinIO
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .stream(inputStream, -1L, 10485760L)
                            .contentType(getMimeTypeFromPath(path))
                            .build()
            );
            // Получаем информацию о файле
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .build()
            );

            log.info("Файл сохранен в MinIO: {}, с размером: {} байт", path, stat.size());
            return path;
        } catch (IllegalArgumentException e) {
            throw new UnknownMimeTypePhotoException(getMimeTypeFromPath(path));
        }
        catch (Exception e) {
            log.error("Ошибка сохранения файла в MinIO: {}", path, e);
            throw wrapException(e, path);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.warn("Ошибка закрытия входного потока: {}", path, e);
            }
        }
    }

    @Override
    public Optional<InputStream> get(String path) {
        validatePath(path);
        try {
            log.debug("Получение файла из MinIO: {}", path);

            // Проверяем существование файла
            if (!exists(path)) {
                log.debug("Файл не найден в  MinIO: {}", path);
                return Optional.empty();
            }
            // Получаем файл
            GetObjectResponse response = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .build()
            );

            return Optional.of(response);

        } catch (Exception e) {
            log.error("Ошибка получения файла в MinIO: {}", path, e);
            throw wrapException(e, path);
        }
    }

    @Override
    public boolean delete(String path) {
        validatePath(path);
        try {
            log.debug("Удаление файла из MinIO: {}", path);

            // Проверяем существование файла
            if (!exists(path)) {
                log.debug("Файл не найден в MinIO: {}", path);
                return false;
            }
            // Удаляем файл
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .build()
            );

            log.info("Файл удален из MinIO: {}", path);
            return true;

        } catch (Exception e) {
            log.error("Ошибка удаления файла из MinIO: {}", path, e);
            throw wrapException(e, path);
        }
    }

    @Override
    public boolean exists(String path) {
        validatePath(path);

        try {
            // Проверяем существование файла через statObject
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .build()
            );
            return true;

        } catch (ErrorResponseException e) {
            // Код 404 означает, что объект не найден
            if (e.errorResponse() != null &&
                    "NoSuchKey".equals(e.errorResponse().code())) {
                return false;
            }
            throw wrapException(e, path);
        } catch (Exception e) {
            log.warn("Ошибка проверки на существования для файла: {}", path, e);
            throw wrapException(e, path);
        }
    }

    @Override
    public String getUrl(String path) {
        validatePath(path);
        return String.format("%s/%s/%s", endpoint, bucket, path);
    }

    @Override
    public String getSignedUrl(String path, Duration timeout) {
        validatePath(path);
        try {
            log.debug("Генерируем подписанный URL для пути: {}, с таймаутом: {}", path, timeout);
            // Генерируем presigned URL
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Http.Method.GET)
                            .bucket(bucket)
                            .object(path)
                            .expiry((int) timeout.getSeconds())
                            .build()
            );

            log.debug("Сгенерированный подписанный URL для пути: {}", path);
            return url;

        } catch (Exception e) {
            log.error("Ошибка генерации подписанного URL для пути: {}", path, e);
            throw wrapException(e, path);
        }
    }

    @Override
    public long getSize(String path) {
        validatePath(path);
        try {
            StatObjectResponse stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .build()
            );
            return stat.size();
        } catch (ErrorResponseException e) {
            if (e.errorResponse() != null &&
                    "NoSuchKey".equals(e.errorResponse().code())) {
                return -1;
            }
            throw wrapException(e, path);
        } catch (Exception e) {
            throw wrapException(e, path);
        }
    }

    @Override
    public String getMimeType(String path) {
        return getMimeTypeFromPath(path);
    }

    // Валидирует путь на безопасность
    private void validatePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Путь пустой или null");
        }

        if (path.contains("..") || path.contains("\0")) {
            throw new IllegalArgumentException("В пути есть недопустимые символы");
        }
    }

    // Определяет MIME-тип по расширению файла
    private String getMimeTypeFromPath(String path) {
        if (path == null) {
            return "unknown";
        }
        String lowerPath = path.toLowerCase();
        if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerPath.endsWith(".png")) {
            return "image/png";
        } else if (lowerPath.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerPath.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerPath.endsWith(".bmp")) {
            return "image/bmp";
        } else if (lowerPath.endsWith(".svg")) {
            return "image/svg+xml";
        } else {
            return "unknown";
        }
    }

    // Оборачивает исключение MinIO в StorageException.
    private StorageException wrapException(Exception e, String path) {
        if (e instanceof StorageException) {
            return (StorageException) e;
        }
        String message = e.getMessage();
        if (e instanceof ErrorResponseException error) {
            if (error.errorResponse() != null) {
                message = String.format(
                        "Ошибка MinIO: %s - %s",
                        error.errorResponse().code(),
                        error.errorResponse().message()
                );
            }
        }

        return new StorageException(message, path);
    }
}
