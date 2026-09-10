package ru.startup.skinscan.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.startup.skinscan.datasource.repository.JpaUserRepository;
import ru.startup.skinscan.security.service.CustomUserDetailsService;
import ru.startup.skinscan.security.exception.CustomAccessDeniedHandler;
import ru.startup.skinscan.security.exception.CustomAuthenticationEntryPoint;
import ru.startup.skinscan.security.filter.RateLimiterFilter;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RateLimiterFilter rateLimiterFilter;
    private final CustomAuthenticationEntryPoint authEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Value("${app.security.cors.allowedOrigins:http://localhost:8080}")
    private String allowedOrigins;

    @Value("${app.security.cors.allowedMethods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.security.cors.allowedHeaders:*}")
    private String allowedHeaders;

    @Value("${app.security.cors.allowCredentials:true}")
    private boolean allowCredentials;

    public SecurityConfig(
            RateLimiterFilter rateLimiterFilter,
            CustomAuthenticationEntryPoint authEntryPoint,
            CustomAccessDeniedHandler accessDeniedHandler
    ) {
        this.rateLimiterFilter = rateLimiterFilter;
        this.authEntryPoint = authEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public UserDetailsService userDetailsService(JpaUserRepository userRepository) {
        return new CustomUserDetailsService(userRepository);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. Отключаем CSRF - для REST API с JWT это не нужно
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Настраиваем CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // 3. Управление сессиями - stateless
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Обработка исключений
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                // 5. Добавляем фильтр Rate Limiting
                .addFilterBefore(rateLimiterFilter, UsernamePasswordAuthenticationFilter.class)

                // 6. Правила авторизации
                .authorizeHttpRequests(auth -> auth
                        // Публичные эндпоинты (доступны всем)
                        .requestMatchers(
                                "/skinScan/register",
                                "/skinScan/login",
                                "/skinScan/check-run",
                                "/swagger-ui/**",
                                "/docs",
                                "/v3/api-docs/**",
                                "/error"
                        ).permitAll()

                        // Эндпоинты для администраторов
                        .requestMatchers(
                                "/skinScan/admin/**"
                        ).hasRole("ADMIN")

                        // Все остальные запросы требуют аутентификации
                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults());

        return http.build();
    }

    //Тестовый профиль - открывает все эндпоинты для QA
    @Bean
    @Profile("test")
    @Order(2)
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                );

        return http.build();
    }

    // Конфигурация CORS
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Разрешенные origin'ы
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // Разрешенные методы
        List<String> methods = Arrays.asList(allowedMethods.split(","));
        configuration.setAllowedMethods(methods);

        // Разрешенные заголовки
        List<String> headers = Arrays.asList(allowedHeaders.split(","));
        configuration.setAllowedHeaders(headers);

        // Разрешить отправку cookies
        configuration.setAllowCredentials(allowCredentials);

        // Время жизни preflight запроса (в секундах)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
