package ru.startup.skinscan.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger log = LoggerFactory.getLogger(AsyncConfig.class);

    @Value("${app.async.core-pool-size:5}")
    private int corePoolSize;

    @Value("${app.async.max-pool-size:10}")
    private int maxPoolSize;

    @Value("${app.async.queue-capacity:100}")
    private int queueCapacity;

    @Value("${app.async.thread-name-prefix:PhotoProcessor-}")
    private String threadNamePrefix;

    @Value("${app.async.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    // Основной пул потоков для обработки фото.
    @Bean(name = "photoTaskExecutor")
    @Primary
    public Executor photoTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // Базовые настройки пула
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setKeepAliveSeconds(keepAliveSeconds);

        // Стратегия при переполнении: выполнять в вызывающем потоке
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        // Настройка для правильного завершения
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        // Инициализируем пул
        executor.initialize();

        log.info("Photo Task Executor запущен: базовое кол-во потоков={}, максимальное кол-во потоков={}, размер очереди ожидания={}",
                corePoolSize, maxPoolSize, queueCapacity);

        return executor;
    }

    //Дополнительный пул для шедулера, используется для восстановления зависших задач.
    @Bean(name = "recoveryTaskExecutor")
    public Executor recoveryTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("Recovery-");
        executor.setKeepAliveSeconds(30);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.initialize();

        log.info("Recovery Task Executor запущен");
        return executor;
    }
}
