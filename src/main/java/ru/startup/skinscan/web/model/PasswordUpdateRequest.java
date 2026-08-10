package ru.startup.skinscan.web.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PasswordUpdateRequest {

    @Size(min = 6, message = "Пароль должен содержать минимум 6 символов")
    private final String newPassword;

    @NotBlank(message = "Старый пароль не может быть пустым")
    private final String oldPassword;

    public PasswordUpdateRequest(String newPassword, String oldPassword) {
        this.newPassword = newPassword;
        this.oldPassword = oldPassword;
    }

    public String newPassword() {
        return newPassword;
    }

    public String oldPassword() {
        return oldPassword;
    }
}
