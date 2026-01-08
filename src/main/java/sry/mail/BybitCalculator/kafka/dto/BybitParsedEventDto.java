package sry.mail.BybitCalculator.kafka.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@Jacksonized
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BybitParsedEventDto {

    public static final String TOPIC_NAME = "bybit-parsed-events";
    public static final int PARTITIONS_COUNT = 8;

    String symbol;
    BigDecimal price;
    OffsetDateTime timestamp;
}
