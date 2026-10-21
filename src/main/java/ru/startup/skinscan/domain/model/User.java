package ru.startup.skinscan.domain.model;

import lombok.Setter;
import ru.startup.skinscan.domain.model.role.UserRole;

import java.util.List;

public class User {

    private final String login;
    @Setter
    private String password;
    @Setter
    private String email;
    @Setter
    private String phone;
    @Setter
    private List<UserRole> roles;

    public User(String login, String password, String email, String phone) {
        this.login = login;
        this.password = password;
        this.email = email;
        this.phone = phone;
    }

    public User(String login, String password, String email, String phone, List<UserRole> roles) {
        this(login, password, email, phone);
        this.roles = List.copyOf(roles);
    }

    public User(String login, String password) {
        this(login, password, null, null);
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

    public List<UserRole> roles() {
        return roles;
    }
}
