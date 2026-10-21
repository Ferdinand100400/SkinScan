package ru.startup.skinscan.web.model.jwt;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JwtResponse {

    private static final String TOKEN_TYPE = "Bearer";

    @JsonProperty("type")
    private final String type;
    @JsonProperty("accessToken")
    private String accessToken;
    @JsonProperty("refreshToken")
    private String refreshToken;

    public JwtResponse() {
        this.type = TOKEN_TYPE;
    }

    public JwtResponse(String accessToken, String refreshToken) {
        this.type = TOKEN_TYPE;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public String type() {
        return type;
    }

    public String accessToken() {
        return accessToken;
    }

    public String refreshToken() {
        return refreshToken;
    }
}
