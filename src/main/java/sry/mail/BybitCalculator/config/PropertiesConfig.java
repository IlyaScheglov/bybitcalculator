package sry.mail.BybitCalculator.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import sry.mail.BybitCalculator.config.properties.AsyncExecutorProperties;

@Configuration
@EnableConfigurationProperties(AsyncExecutorProperties.class)
public class PropertiesConfig {
}
