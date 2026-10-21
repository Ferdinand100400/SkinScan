package ru.startup.skinscan.web.model.jwt;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class JwtRequest {

    @NotBlank(message = "Логин не может быть пустым")
    @Size(min = 4, max = 50, message = "Логин должен быть от 4 до 50 символов")
    @JsonProperty("login")
    private String login;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 5, max = 20, message = "Пароль должен быть от 5 до 20 символов")
    @JsonProperty("pass")
    private String password;

    public JwtRequest() {
    }

    public JwtRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String login() {
        return login;
    }

    public String password() {
        return password;
    }
}