package sry.mail.BybitCalculator.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Value
@Builder
@Jacksonized
public class UserSettingsResponseDto {

    BigDecimal longPercent;
    Integer longMinutes;
    BigDecimal shortPercent;
    Integer shortMinutes;
    BigDecimal dumpPercent;
    Integer dumpMinutes;
}
