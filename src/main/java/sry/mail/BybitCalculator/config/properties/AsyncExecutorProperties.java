package sry.mail.BybitCalculator.config.properties;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@ConfigurationProperties(prefix = "async-executor")
public class AsyncExecutorProperties {

    Integer corePoolSize;
    Integer maxPoolSize;
    Integer queueCapacity;
}
