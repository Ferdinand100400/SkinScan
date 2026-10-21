package ru.startup.skinscan.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class UserResponse {

    @JsonProperty("id")
    private UUID id;
    @JsonProperty("login")
    private String login;
    @JsonProperty("email")
    private String email;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("roles")
    private List<String> roles;
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt;

    public UserResponse() {
    }

    public UserResponse(UUID id, String login, String email, String phone, List<String> roles, OffsetDateTime createdAt) {
        this.id = id;
        this.login = login;
        this.email = email;
        this.phone = phone;
        this.roles = roles;
        this.createdAt = createdAt;
    }

    public UUID id() {
        return id;
    }

    public String login() {
        return login;
    }

    public String email() {
        return email;
    }

    public String phone() {
        return phone;
    }

    public List<String> roles() {
        return roles;
    }

    public OffsetDateTime createdAt() {
        return createdAt;
    }
}
