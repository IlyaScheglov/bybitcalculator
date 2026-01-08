package sry.mail.BybitCalculator.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.ProducerFactory;
import sry.mail.BybitCalculator.kafka.dto.BybitParsedEventDto;
import sry.mail.BybitCalculator.kafka.dto.UserNotificationEventDto;

@Configuration
public class KafkaConfig {

    @Bean
    public KafkaTemplateAdapter kafkaTemplateAdapter(ProducerFactory<String, String> producerFactory,
                                                     ObjectMapper objectMapper) {
        return new KafkaTemplateAdapter(producerFactory, objectMapper);
    }

    @Bean
    public KafkaAdmin.NewTopics newTopics() {
        return new KafkaAdmin.NewTopics(
                TopicBuilder.name(UserNotificationEventDto.TOPIC_NAME)
                        .partitions(UserNotificationEventDto.PARTITIONS_COUNT)
                        .replicas(1)
                        .build()
        );
    }
}
