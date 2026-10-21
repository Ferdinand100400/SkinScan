package ru.startup.skinscan.domain.model.jwt;

public class JwtTokens {

    private final String accessToken;
    private final String refreshToken;

    public JwtTokens(String accessToken, String refreshToken) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String accessToken() {
        return accessToken;
    }

    public String refreshToken() {
        return refreshToken;
    }
}