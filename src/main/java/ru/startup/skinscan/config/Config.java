package ru.startup.skinscan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.startup.skinscan.datasource.repository.JpaUserRepository;
import ru.startup.skinscan.domain.service.UserService;
import ru.startup.skinscan.domain.service.UserServiceImpl;

@Component
public class Config {

    @Bean
    UserService userService(JpaUserRepository jpaUserRepository, PasswordEncoder passwordEncoder) {
        return new UserServiceImpl(jpaUserRepository, passwordEncoder);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
