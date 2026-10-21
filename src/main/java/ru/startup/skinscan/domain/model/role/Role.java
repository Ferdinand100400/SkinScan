package ru.startup.skinscan.domain.model.role;

public enum Role {
    USER;

    private static final String PREFIX = "ROLE_";

    public String getAuthority() {
        return PREFIX + name();
    }

    public static Role fromAuthority(String authority) {
        if (authority == null || !authority.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Некорректная роль: " + authority);
        }
        return Role.valueOf(authority.substring(PREFIX.length()));
    }
}