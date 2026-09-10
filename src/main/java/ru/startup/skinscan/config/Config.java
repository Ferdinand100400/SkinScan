package ru.startup.skinscan.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.startup.skinscan.datasource.repository.JpaUserRepository;
import ru.startup.skinscan.domain.service.*;

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

    @Bean
    @Profile("local")
    @Primary
    public FileStorageService localFileStorageService() {
        return new LocalFileStorageService();
    }

    @Bean
    @Profile("minio")
    @Primary
    public FileStorageService minioFileStorageService() {
        return new MinioFileStorageService();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new ParameterNamesModule())
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }
}
