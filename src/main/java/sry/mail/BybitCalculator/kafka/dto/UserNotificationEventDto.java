package sry.mail.BybitCalculator.kafka.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserNotificationEventDto {

    public static final String TOPIC_NAME = "bybit-bot-user-notification-events";
    public static final int PARTITIONS_COUNT = 8;

    String tgId;
    String symbol;
    NotificationType type;
}
