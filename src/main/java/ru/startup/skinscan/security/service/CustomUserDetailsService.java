package ru.startup.skinscan.security.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.startup.skinscan.datasource.entity.UserEntity;
import ru.startup.skinscan.datasource.repository.JpaUserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final JpaUserRepository userRepository;

    public CustomUserDetailsService(JpaUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        log.info("Поиск пользователя с логином: {}", login);  // ← добавить

        UserEntity user = userRepository.findByLogin(login)
                .orElseThrow(() -> {
                    log.error("Пользователь с логином {} не найден в БД", login);
                    return new UsernameNotFoundException("User not found: " + login);
                });

        log.info("Найден пользователь: {}, пароль: {}", user.login(), user.password());

        return User.builder()
                .username(user.login())
                .password(user.password())
                .build();
    }
}
