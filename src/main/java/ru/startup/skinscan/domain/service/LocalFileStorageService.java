package ru.startup.skinscan.domain.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import ru.startup.skinscan.exception.StorageException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.time.Duration;
import java.util.Optional;

public class LocalFileStorageService implements FileStorageService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStorageService.class);
    private static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();
    private static final String TEMP_DIR = "temp";

    @Value("${app.storage.local.path:${user.home}/data/uploads}")
    private String basePath;

    @Value("${app.storage.local.create-on-start:true}")
    private boolean createOnStart;

    @Value("${app.storage.local.file-permissions:755}")
    private String filePermissions;

    @PostConstruct
    public void init() {
        log.info("Инициализация локального хранилища фотографий с начальным путем: {}", basePath);
        Path baseDir = Paths.get(basePath);
        // Создаем базовую директорию если нужно
        if (createOnStart && !Files.exists(baseDir)) {
            try {
                Files.createDirectories(baseDir);
                log.info("Создана начальная директория: {}", basePath);
            } catch (IOException e) {
                throw new StorageException(
                        "Ошибка создания начальной директории",
                        basePath
                );
            }
        }
        // Проверяем права на запись
        if (!Files.isWritable(baseDir)) {
            throw new StorageException(
                    "Base directory is not writable",
                    basePath
            );
        }
        // Создаем временную директорию
        Path tempDir = baseDir.resolve(TEMP_DIR);
        if (!Files.exists(tempDir)) {
            try {
                Files.createDirectories(tempDir);
                log.info("Создана временная директория: {}", tempDir);
            } catch (IOException e) {
                log.warn("Ошибка создания временной директории: {}", tempDir, e);
            }
        }

        log.info("Инициализация локального хранилища фотографий успешна завершена!");
    }

    @Override
    public String save(InputStream inputStream, String path) {
        validatePath(path);
        Path targetPath = resolvePath(path);
        Path tempPath = resolveTempPath(path);

        log.debug("Файл сохранится в директории: {}, с временной директорией: {}", targetPath, tempPath);

        try {
            // Создаем родительские директории
            Files.createDirectories(targetPath.getParent());
            Files.createDirectories(tempPath.getParent());
            // Сохраняем во временный файл (атомарная операция)
            Files.copy(inputStream, tempPath, StandardCopyOption.REPLACE_EXISTING);
            // Проверяем, что файл записан корректно
            if (!Files.exists(tempPath) || Files.size(tempPath) == 0) {
                throw new StorageException(
                        "Ошибка записи файла",
                        path
                );
            }
            // Атомарно перемещаем в целевое место
            Files.move(tempPath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            // Устанавливаем права доступа (если ОС поддерживает)
            setFilePermissions(targetPath);

            log.info("Файл успешно сохранен: {}, с размером: {} байт",
                    path, Files.size(targetPath));

            return path;

        } catch (IOException e) {
            // Удаляем временный файл в случае ошибки
            try {
                Files.deleteIfExists(tempPath);
            } catch (IOException errorDelete) {
                log.warn("Ошибка удаления временного файла: {}", tempPath, errorDelete);
            }
            throw new StorageException(
                    "Ошибка сохранения файла",
                    targetPath.toString()
            );
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.warn("Ошибка закрытия входного потока для: {}", path, e);
            }
        }
    }

    @Override
    public Optional<InputStream> get(String path) {
        validatePath(path);
        Path filePath = resolvePath(path);

        if (!Files.exists(filePath)) {
            log.debug("Файл не найден по пути: {}", filePath);
            return Optional.empty();
        }

        try {
            InputStream inputStream = Files.newInputStream(filePath);
            return Optional.of(inputStream);
        } catch (IOException e) {
            throw new StorageException(
                    "Ошибка чтения из файла",
                    path
            );
        }
    }

    @Override
    public boolean delete(String path) {
        validatePath(path);
        Path filePath = resolvePath(path);

        if (!Files.exists(filePath)) {
            log.debug("Файл не найден по пути: {}", filePath);
            return false;
        }

        try {
            Files.delete(filePath);
            log.info("Файл успешно удален по пути: {}", filePath);
            // Удаляем пустые родительские директории
            cleanEmptyDirectories(filePath.getParent());
            return true;
        } catch (IOException e) {
            throw new StorageException(
                    "Ошибка удаления файла",
                    filePath.toString()
            );
        }
    }

    @Override
    public boolean exists(String path) {
        validatePath(path);
        return Files.exists(resolvePath(path));
    }

    @Override
    public String getUrl(String path) {
        validatePath(path);
        return "/skinScan/photos/" + path;
    }

    @Override
    public String getSignedUrl(String path, Duration timeout) {
        log.warn("Подписанный URLs для локального хранилища не поддерживается, обычный URL для пути: {}", path);
        return getUrl(path);
    }

    @Override
    public long getSize(String path) {
        validatePath(path);
        Path filePath = resolvePath(path);
        if (!Files.exists(filePath)) {
            return -1;
        }
        try {
            return Files.size(filePath);
        } catch (IOException e) {
            throw new StorageException(
                    "Ошибка получения размера файла",
                    filePath.toString()
            );
        }
    }

    @Override
    public String getMimeType(String path) {
        validatePath(path);
        Path filePath = resolvePath(path);
        if (!Files.exists(filePath)) {
            return "Unknown";
        }
        try {
            String mimeType = Files.probeContentType(filePath);
            return mimeType != null ? mimeType : "Unknown";
        } catch (IOException e) {
            log.warn("Ошибка получения типа файла по пути: {}", path, e);
            return "Unknown";
        }
    }


    // Валидирует путь на правильность. Защищает от path traversal атак (../).
    private void validatePath(String path) {
        if (path == null || path.trim().isEmpty()) {
            throw new IllegalArgumentException("Путь пустой или null");
        }
        // Проверяем на попытку выйти за пределы базовой директории
        Path normalized = Paths.get(path).normalize();
        if (normalized.toString().contains("..")) {
            throw new IllegalArgumentException("В пути не должно быть: '..'");
        }
        // Проверяем на запрещенные символы
        if (path.contains("\0")) {
            throw new IllegalArgumentException("В пути есть запрещенные символы");
        }
    }

    // Соединяет базовый путь и переданный
    private Path resolvePath(String path) {
        Path baseDir = Paths.get(basePath).toAbsolutePath().normalize();
        Path normalized = Paths.get(path).normalize();
        // Проверяем, что путь не выходит за пределы базовой директории
        Path resolved = baseDir.resolve(normalized).normalize();
        if (!resolved.startsWith(baseDir.normalize()))
            throw new IllegalArgumentException("Полученный путь: " + path + " выходит за границы базового: " + basePath);

        return resolved;
    }

    // Генерирует путь для временного файла в той же директории.
    private Path resolveTempPath(String path) {
        Path parent = resolvePath(path).getParent();
        String fileName = resolvePath(path).getFileName().toString();
        String tempFileName = "." + fileName + ".tmp";
        return parent.resolve(TEMP_DIR).resolve(tempFileName);
    }

    // Устанавливает права доступа к файлу (для Unix-систем)
    private void setFilePermissions(Path path) {
        try {
            // Проверяем, поддерживает ли файловая система права доступа
            if (!path.getFileSystem().supportedFileAttributeViews().contains("posix")) {
                return;
            }

            // Парсим права: 755 -> rwxr-xr-x
            if (filePermissions != null && filePermissions.length() == 3) {
                String perms = filePermissions;
                StringBuilder posixPerms = new StringBuilder();

                for (char c : perms.toCharArray()) {
                    int perm = Character.getNumericValue(c);
                    if (perm >= 4) posixPerms.append('r'); else posixPerms.append('-');
                    if (perm >= 2) posixPerms.append('w'); else posixPerms.append('-');
                    if (perm >= 1) posixPerms.append('x'); else posixPerms.append('-');
                }

                Files.setPosixFilePermissions(path,
                        PosixFilePermissions.fromString(posixPerms.toString()));

                log.debug("Установлены права {} ({}) для файла: {}",
                        filePermissions, posixPerms, path);
            }
        } catch (UnsupportedOperationException e) {
            log.debug("Файловая система не поддерживает POSIX права");
        } catch (Exception e) {
            log.warn("Ошибка установки прав для файла по пути: {}", path, e);
        }
    }

    // Рекурсивно удаляет пустые родительские директории
    private void cleanEmptyDirectories(Path dir) {
        if (dir == null || !Files.exists(dir)) {
            return;
        }
        try {
            // Проверяем, что директория пуста
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                if (!stream.iterator().hasNext()) {
                    Files.delete(dir);
                    log.debug("Пустая директория {} удалена", dir);
                    // Проверяем родительскую директорию
                    cleanEmptyDirectories(dir.getParent());
                }
            }
        } catch (IOException e) {
            log.debug("Ошибка удаления пустых директорий: {}", dir, e);
        }
    }
}
