package ru.startup.skinscan.datasource.entity;

import jakarta.persistence.*;
import lombok.Setter;
import ru.startup.skinscan.domain.model.role.Role;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    private UUID id;

    private String login;

    @Setter
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id")
    )
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();

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

    public UserEntity(UUID id, String login, String password, Set<Role> roles, String email, String phone, OffsetDateTime createdAt, OffsetDateTime updatedAt) {
        this.id = id;
        this.login = login;
        this.password = password;
        this.roles = roles;
        this.email = email;
        this.phone = phone;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UserEntity(String login, String password, Set<Role> roles, String email, String phone) {
        this(UUID.randomUUID(), login, password, roles, email, phone, OffsetDateTime.now(), OffsetDateTime.now());
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

    public Set<Role> roles() {
        return roles;
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
