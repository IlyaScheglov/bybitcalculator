package sry.mail.BybitCalculator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import sry.mail.BybitCalculator.config.properties.AsyncExecutorProperties;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncExecutorConfig {

    @Bean("asyncExecutor")
    public Executor executor(AsyncExecutorProperties asyncExecutorProperties) {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncExecutorProperties.getCorePoolSize());
        executor.setMaxPoolSize(asyncExecutorProperties.getMaxPoolSize());
        executor.setQueueCapacity(asyncExecutorProperties.getQueueCapacity());
        executor.initialize();
        return executor;
    }
}
