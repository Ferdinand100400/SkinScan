package ru.startup.skinscan.domain.service;

import org.springframework.stereotype.Service;
import ru.startup.skinscan.domain.model.User;

import java.util.UUID;

@Service
public interface AuthService {
    UUID register(User user);
    UUID validate(String base64);
}
