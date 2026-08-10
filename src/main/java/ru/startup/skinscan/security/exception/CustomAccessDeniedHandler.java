package ru.startup.skinscan.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Обработчик для случаев, когда пользователь аутентифицирован, но не имеет прав доступа к ресурсу.
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("время", LocalDateTime.now().toString());
        errorResponse.put("статус", HttpStatus.FORBIDDEN.value());
        errorResponse.put("ошибка", "Нет права доступа");
        errorResponse.put("сообщение", "Нет прав к этому ресурсу");
        errorResponse.put("эндпоинт", request.getRequestURI());

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
