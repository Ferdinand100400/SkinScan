package ru.startup.skinscan.ML.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.startup.skinscan.ML.MLClient;
import ru.startup.skinscan.ML.StubMLClient;

@Configuration
public class MLConfig {

    @Bean
    public MLClient mlClient() {
        return new StubMLClient();
    }

}
