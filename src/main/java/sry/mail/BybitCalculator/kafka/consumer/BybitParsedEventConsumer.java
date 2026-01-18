package sry.mail.BybitCalculator.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import sry.mail.BybitCalculator.kafka.dto.BybitParsedEventDto;
import sry.mail.BybitCalculator.service.ChartService;

@Slf4j
@Component
@RequiredArgsConstructor
public class BybitParsedEventConsumer {

    private final ObjectMapper objectMapper;
    private final ChartService chartService;

    @KafkaListener(topics = BybitParsedEventDto.TOPIC_NAME, concurrency = "4")
    public void processBybitParserEventMessage(String message) {
        try {
            var payload = objectMapper.readValue(message, BybitParsedEventDto.class);
            log.info("Receive bybit parsed event with payload {}", payload);
            chartService.saveNewChart(payload);
            log.info("Successfully saving chart to db");
        } catch (Exception ex) {
            log.error("Error process bybit parser message", ex);
        }
    }
}
