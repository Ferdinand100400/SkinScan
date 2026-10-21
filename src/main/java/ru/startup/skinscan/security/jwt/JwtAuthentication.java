package ru.startup.skinscan.security.jwt;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import ru.startup.skinscan.domain.model.role.UserRole;

import java.util.List;
import java.util.UUID;

public class JwtAuthentication extends AbstractAuthenticationToken {

    private final UUID principal;
    private final String token;
    private final boolean authenticated;

    // Конструктор для аутентифицированного пользователя, вызывается после успешной валидации токена.
    public JwtAuthentication(UUID principal, List<UserRole> authorities, String token) {
        super(authorities);
        this.principal = principal;
        this.token = token;
        this.authenticated = true;
        setAuthenticated(true);
    }

    // Конструктор для неаутентифицированного пользователя, используется, если нужно создать объект до валидации
    public JwtAuthentication(String token) {
        super(List.of());
        this.principal = null;
        this.token = token;
        this.authenticated = false;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public String getName() {
        return principal != null ? principal.toString() : "";
    }

    public String getToken() {
        return token;
    }

    public boolean isAuthenticatedFlag() {
        return authenticated;
    }

    @Override
    public String toString() {
        return "JwtAuthentication{" +
                "principal=" + principal +
                ", authenticated=" + authenticated +
                ", authorities=" + getAuthorities() +
                '}';
    }
}
