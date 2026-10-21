package ru.startup.skinscan.security.filter;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.startup.skinscan.security.exception.CustomAuthenticationEntryPoint;
import ru.startup.skinscan.security.jwt.JwtAuthentication;
import ru.startup.skinscan.security.jwt.JwtProvider;
import ru.startup.skinscan.security.jwt.JwtUtil;
import ru.startup.skinscan.security.rateLimiting.RateLimiter;

import java.io.IOException;
import java.util.UUID;


// Фильтр для применения Rate Limiting к запросам регистрации.
@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimiterFilter.class);

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final RateLimiter rateLimiter;
    private final JwtProvider jwtProvider;
    private final JwtUtil jwtUtil;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;

    @Value("${app.rateLimiting.register.limit:5}")
    private int registerLimit;

    @Value("${app.rateLimiting.register.windowSeconds:60}")
    private long registerWindowSeconds;

    @Value("${app.rateLimiting.login.limit:10}")
    private int loginLimit;

    @Value("${app.rateLimiting.login.windowSeconds:60}")
    private long loginWindowSeconds;

    public RateLimiterFilter(RateLimiter rateLimiter, JwtProvider jwtProvider, JwtUtil jwtUtil, CustomAuthenticationEntryPoint customAuthenticationEntryPoint) {
        this.rateLimiter = rateLimiter;
        this.jwtProvider = jwtProvider;
        this.jwtUtil = jwtUtil;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Если не открытые эндпоинты, то читаем заголовок
        if (!isPublicPath(path)) {
            String header = request.getHeader(AUTH_HEADER);
            if (header == null || header.isEmpty()) {
                log.warn("AuthFilter: отсутствует или некорректен заголовок Authorization для {}", path);
                customAuthenticationEntryPoint.commence(request, response, null);
                return;
            }
            if (header.startsWith(BEARER_PREFIX)) {
                header = header.substring(BEARER_PREFIX.length());
            }
            if (!jwtProvider.validateAccessToken(header)) {
                log.warn("AuthFilter: невалидный access-токен для {}", path);
                customAuthenticationEntryPoint.commence(request, response, null);
            }
            try {
                Claims claims = jwtProvider.getClaims(header);
                JwtAuthentication jwtAuthentication = jwtUtil.createAuthentication(claims, header);
                UUID userId = (UUID) jwtAuthentication.getPrincipal();
                request.setAttribute("userId", userId);
                log.info("Установлен атрибут userId: {} для эндпоинта {}", userId, request.getRequestURI());

                SecurityContextHolder.getContext().setAuthentication(jwtAuthentication);
                log.debug("AuthFilter: аутентификация установлена для {}", path);
                log.info("Эндпоинт {} - фильтр пропускает к контроллеру", path);
            } catch (Exception e) {
                log.warn("Эндпоинт {} - не прошел аутентификацию, фильтр не пропускает к контроллеру", path);
                customAuthenticationEntryPoint.commence(request, response, null);
            }
        }

        // Проверяем, нужно ли применять Rate Limiting к этому запросу
        if (!isRateLimitedPath(path)) {
            filterChain.doFilter(request, response);
            log.info("После doFilter без Rate Limiting");
            return;
        }

        // Определяем ключ для Rate Limiting (комбинация IP + User-Agent)
        String key = buildRateLimitKey(request);

        // Определяем настройки лимита в зависимости от эндпоинта
        int limit = getLimitForPath(path);
        long windowSeconds = getWindowForPath(path);

        // Проверяем, разрешен ли запрос
        boolean allowed = rateLimiter.isAllowed(key, limit, windowSeconds);

        // Добавляем заголовки о состоянии лимитов
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(
                rateLimiter.getRemainingTokens(key)
        ));
        response.setHeader("X-RateLimit-Reset", String.valueOf(
                rateLimiter.getResetTimeSeconds(key)
        ));

        if (!allowed) {
            log.warn("Превышен лимит для ключа: {}, path: {}", key, path);

            // Возвращаем 429 Too Many Requests
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After",
                    String.valueOf(rateLimiter.getResetTimeSeconds(key))
            );

            // Отправляем JSON-ответ с ошибкой
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                    "{\"Ошибка\":\"Много запросов\",\"сообщение\":\"Лимит скорости превышен. Повторите попытку через %d секунд.\"}",
                    rateLimiter.getResetTimeSeconds(key)
            ));
            return;
        }

        // Пропускаем запрос дальше
        filterChain.doFilter(request, response);
        log.info("После doFilter и Rate Limiting");
    }

    // Определяем, нужно ли применять Rate Limiting к данному пути
    private boolean isRateLimitedPath(String path) {
        return path.matches("^/skinScan/(register|login).*$");
    }

    // Определяем, публичные пути
    private boolean isPublicPath(String path) {
        return path.matches("^/skinScan/(register|login|check-run|refresh-token/*).*$") ||
                path.matches("^/swagger-ui/*$") ||
                path.matches("^/docs") ||
                path.matches("^/v3/api-docs/*$");
    }

    /**
     * Строим ключ для Rate Limiting на основе IP и User-Agent.
     * Это защищает от обхода через прокси/VPN (разные IP, но одинаковый User-Agent).
     */
    private String buildRateLimitKey(HttpServletRequest request) {
        String ip = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");
        String key = ip;

        if (userAgent != null && !userAgent.isEmpty()) {
            // Используем хэш User-Agent для экономии памяти
            key = ip + ":" + userAgent.hashCode();
        }

        log.debug("Ключ ограничения: {}", key);
        return key;
    }

     /*
     Получает реальный IP клиента, учитывая прокси.
     Проверяет заголовки в порядке приоритета:
     1. X-Forwarded-For (стандарт для прокси)
     2. X-Real-IP (NGINX)
     3. Proxy-Client-IP (Apache)
     4. WL-Proxy-Client-IP (WebLogic)
     */
    private String getClientIp(HttpServletRequest request) {
        String[] headers = {
                "X-Forwarded-For",
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For может содержать несколько IP (клиент, прокси1, прокси2)
                // Берем первый - это реальный клиент
                if (header.equals("X-Forwarded-For")) {
                    String[] ips = ip.split(",");
                    return ips[0].trim();
                }
                return ip;
            }
        }

        return request.getRemoteAddr();
    }

    private int getLimitForPath(String path) {
        if (path.contains("/register")) {
            return registerLimit;
        }
        if (path.contains("/login")) {
            return loginLimit;
        }
        return registerLimit; // Значение по умолчанию
    }

    private long getWindowForPath(String path) {
        if (path.contains("/register")) {
            return registerWindowSeconds;
        }
        if (path.contains("/login")) {
            return loginWindowSeconds;
        }
        return registerWindowSeconds;
    }
}
