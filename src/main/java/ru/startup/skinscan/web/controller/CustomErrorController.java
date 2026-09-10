package ru.startup.skinscan.web.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class CustomErrorController implements ErrorController {

    private final Environment env;

    public CustomErrorController(Environment env) {
        this.env = env;
    }

    @RequestMapping("/error")
    public ResponseEntity<Map<String, Object>> handleError(
            HttpServletRequest request) {

        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);

        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("Время ошибки", LocalDateTime.now().toString());
        response.put("Код ошибки", statusCode);
        response.put("Ошибка", getErrorName(statusCode));
        response.put("Эндпоинт", request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI));

        if (isDevelopment()) {
            response.put("Сообщение ошибки", message);
            response.put("Ошибка", request.getAttribute(RequestDispatcher.ERROR_EXCEPTION));
        }

        return ResponseEntity.status(statusCode).body(response);
    }

    private String getErrorName(int status) {
        return switch (status) {
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized";
            case 403 -> "Forbidden";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default -> "Error";
        };
    }

    private boolean isDevelopment() {
        return Arrays.asList(env.getActiveProfiles()).contains("dev")
                || Arrays.asList(env.getActiveProfiles()).contains("local")
                || Arrays.asList(env.getActiveProfiles()).contains("minio");
    }
}
