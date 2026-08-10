package ru.startup.skinscan.web.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRequest {

    @NotBlank(message = "Логин не может быть пустым")
    private final String login;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    private final String password;

    @Email(message = "Неверный формат email")
    private final String email;

    private final String phone;

    public UserRequest(String login, String password, String email, String phone) {
        this.login = login;
        this.password = password;
        this.email = email;
        this.phone = phone;
    }

    public String login() {
        return login;
    }

    public String password() {
        return password;
    }

    public String email() {
        return email;
    }

    public String phone() {
        return phone;
    }
}
