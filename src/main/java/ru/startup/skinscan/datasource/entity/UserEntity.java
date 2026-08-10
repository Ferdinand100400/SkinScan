package ru.startup.skinscan.datasource.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private UUID id;

    private String login;

    @Setter
    private String password;

    @Setter
    private String email;

    @Setter
    private String phone;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Setter
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public UserEntity() {
    }

    public UserEntity(UUID id, String login, String password, String email, String phone, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UserEntity(String login, String password, String email, String phone) {
        this(UUID.randomUUID(), login, password, email, phone, LocalDateTime.now(), LocalDateTime.now());
    }

    public UUID id() {
        return id;
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
