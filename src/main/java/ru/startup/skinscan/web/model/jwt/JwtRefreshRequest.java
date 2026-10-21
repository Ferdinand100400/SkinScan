package ru.startup.skinscan.web.model.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public class JwtRefreshRequest {
    @NotBlank(message = "Refresh-токен не может быть пустым")
    @JsonProperty("refreshToken")
    private String refreshToken;

    public JwtRefreshRequest() {
    }

    public JwtRefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String refreshToken() {
        return refreshToken;
    }
}
