package ru.startup.skinscan.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Обработчик для случаев, когда пользователь не аутентифицирован
// т.к. Spring Security по умолчанию возвращает 401 с пустым телом
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("время", LocalDateTime.now().toString());
        errorResponse.put("статус", HttpStatus.UNAUTHORIZED.value());
        errorResponse.put("ошибка", "авторизации");
        errorResponse.put("сообщение", "Для доступа к этому ресурсу требуется аутентификация");
        errorResponse.put("эндпоинт", request.getRequestURI());

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
