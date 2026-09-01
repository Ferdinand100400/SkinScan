package ru.startup.skinscan.datasource.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Setter;

import java.time.OffsetDateTime;
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
    private OffsetDateTime createdAt;

    @Setter
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    public UserEntity() {
    }

    public UserEntity(UUID id, String login, String password, String email, String phone, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UserEntity(String login, String password, String email, String phone) {
        this(UUID.randomUUID(), login, password, email, phone, OffsetDateTime.now(), OffsetDateTime.now());
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

    public OffsetDateTime createdAt() {
        return createdAt;
    }

    public OffsetDateTime updatedAt() {
        return updatedAt;
    }
}
