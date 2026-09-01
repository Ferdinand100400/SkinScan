package ru.startup.skinscan.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcLoggingFilter extends OncePerRequestFilter {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String SESSION_ID_HEADER = "X-Session-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Генерируем requestId (берем из заголовка, если есть, или создаем новый)
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isEmpty()) {
            requestId = UUID.randomUUID().toString();
        }

        // 2. Пробуем получить sessionId (если есть)
        String sessionId = request.getHeader(SESSION_ID_HEADER);
        if (sessionId == null || sessionId.isEmpty()) {
            // Можно взять из HttpSession, если она создана
            var httpSession = request.getSession(false);
            if (httpSession != null) {
                sessionId = httpSession.getId();
            } else {
                sessionId = "NO_SESSION";
            }
        }

        // 3. Кладем значения в MDC
        MDC.put("requestId", requestId);
        MDC.put("sessionId", sessionId);

        // 4. Добавляем requestId в ответ (чтобы клиент мог его использовать)
        response.setHeader(REQUEST_ID_HEADER, requestId);

        try {
            // 5. Пропускаем запрос дальше по цепочке фильтров
            filterChain.doFilter(request, response);
        } finally {
            // 6. Очищаем MDC после обработки запроса (ВАЖНО!)
            MDC.clear();
        }
    }
}