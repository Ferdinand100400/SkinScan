package ru.startup.skinscan.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.startup.skinscan.domain.model.User;
import ru.startup.skinscan.domain.model.role.UserRole;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtProvider.class);
    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLES = "roles";
    private static final String CLAIM_TYPE = "type";

    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessExpiration;
    private final long refreshExpiration;
    private final String issuer;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access.expiration}") long accessExpiration,
            @Value("${jwt.refresh.expiration}") long refreshExpiration,
            @Value("${jwt.issuer}") String issuer
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
        this.issuer = issuer;
    }

    // Генерация Access-токена
    public String generateAccessToken(User user, UUID userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessExpiration);

        List<String> roles = user.roles().stream()
                .map(UserRole::getAuthority)
                .toList();

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim(CLAIM_USER_ID, userId.toString())
                .claim(CLAIM_ROLES, roles)
                .claim(CLAIM_TYPE, TYPE_ACCESS)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Генерация Refresh-токена
    public String generateRefreshToken(UUID userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .setSubject(userId.toString())
                .claim(CLAIM_USER_ID, userId.toString())
                .claim(CLAIM_TYPE, TYPE_REFRESH)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Валидация Access-токена
    public boolean validateAccessToken(String token) {
        return validate(token, TYPE_ACCESS);
    }

    // Валидация Refresh-токена
    public boolean validateRefreshToken(String token) {
        return validate(token, TYPE_REFRESH);
    }

    private boolean validate(String token, String expectedType) {
        if (token == null || token.isBlank()) {
            log.debug("JWT валидация: токен пустой");
            return false;
        }
        try {
            Claims claims = getClaims(token);
            String type = claims.get(CLAIM_TYPE, String.class);
            if (!expectedType.equals(type)) {
                log.debug("JWT валидация: ожидался тип '{}', получен '{}'", expectedType, type);
                return false;
            }
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("JWT валидация: токен истёк");
        } catch (SignatureException e) {
            log.warn("JWT валидация: неверная подпись токена");
        } catch (MalformedJwtException e) {
            log.warn("JWT валидация: токен повреждён");
        } catch (UnsupportedJwtException e) {
            log.warn("JWT валидация: неподдерживаемый токен");
        } catch (IllegalArgumentException e) {
            log.warn("JWT валидация: некорректный аргумент");
        }
        return false;
    }

    public Claims getClaims(String token) {
        Jws<Claims> jws = Jwts.parser()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
        return jws.getBody();
    }

    public UUID getUserId(Claims claims) {
        String userId = claims.get(CLAIM_USER_ID, String.class);
        return UUID.fromString(userId);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRoles(Claims claims) {
        Object roles = claims.get(CLAIM_ROLES);
        if (roles instanceof List<?> list) {
            return (List<String>) list;
        }
        return List.of();
    }

}
