package ru.startup.skinscan.domain.model.role;

import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

public class UserRole implements GrantedAuthority {

    private final Role role;

    public UserRole(Role role) {
        this.role = role;
    }

    public UserRole(String authority) {
        this.role = Role.fromAuthority(authority);
    }

    @Override
    public String getAuthority() {
        return role().getAuthority();
    }

    public Role role() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRole userRole)) return false;
        return role == userRole.role;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(role);
    }

    @Override
    public String toString() {
        return getAuthority();
    }
}