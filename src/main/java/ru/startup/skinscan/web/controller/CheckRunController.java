package ru.startup.skinscan.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/skinScan")
public class CheckRunController {

    private final JdbcTemplate jdbcTemplate;
    private final String startApp;

    public CheckRunController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        startApp = Instant.now().toString();
    }

    @GetMapping("/check-run")
    public ResponseEntity<Map<String, Object>> checkDatabase() {
        Map<String, Object> response = new LinkedHashMap<>();
        long start = System.currentTimeMillis();
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);

            long duration = System.currentTimeMillis() - start;

            response.put("статус", "Успешно");
            response.put("БД", "PostgreSQL");
            response.put("Результат работы БД", result);
            response.put("Время запроса, мс", duration);
            response.put("Текущее время", Instant.now().toString());
            response.put("Время старта приложения", startApp);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - start;

            response.put("статус", "Ошибка");
            response.put("Ошибка", e.getMessage());
            response.put("Время запроса", duration);

            return ResponseEntity
                    .status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(response);
        }
    }
}
