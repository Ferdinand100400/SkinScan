package ru.startup.skinscan.domain.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.startup.skinscan.domain.model.User;

import java.util.UUID;

@Service
public interface UserService {
    UUID createUser(User user);
    User findUserByLogin(String login);
    UUID findUserIdByLogin(String login);
    UUID updateUser(User user);
    int updatePassword(UUID idUser, String oldPassword, String newPassword);
    PasswordEncoder passwordEncoder();
}
