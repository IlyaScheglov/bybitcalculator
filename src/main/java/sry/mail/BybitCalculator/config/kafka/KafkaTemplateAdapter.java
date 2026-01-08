package sry.mail.BybitCalculator.config.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

public class KafkaTemplateAdapter extends KafkaTemplate<String, String> {

    private final ObjectMapper objectMapper;

    public KafkaTemplateAdapter(ProducerFactory<String, String> producerFactory, ObjectMapper objectMapper) {
        super(producerFactory);
        this.objectMapper = objectMapper;
    }

    @SneakyThrows
    public void sendMessageString(String topicName, Object payload) {
        send(topicName, objectMapper.writeValueAsString(payload));
    }
}
