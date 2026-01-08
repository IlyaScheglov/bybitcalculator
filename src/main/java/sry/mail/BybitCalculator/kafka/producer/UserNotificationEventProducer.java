package sry.mail.BybitCalculator.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import sry.mail.BybitCalculator.config.kafka.KafkaTemplateAdapter;
import sry.mail.BybitCalculator.kafka.dto.UserNotificationEventDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNotificationEventProducer {

    private final KafkaTemplateAdapter kafkaTemplateAdapter;

    public void sendUserNotificationEvent(UserNotificationEventDto payload) {
        kafkaTemplateAdapter.sendMessageString(UserNotificationEventDto.TOPIC_NAME, payload);
        log.info("User notification event was sent with payload : {}", payload);
    }
}
