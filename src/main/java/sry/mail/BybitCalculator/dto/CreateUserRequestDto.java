package sry.mail.BybitCalculator.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Value
@Builder
@Jacksonized
public class CreateUserRequestDto {

    String tgId;
    BigDecimal minPercentOfPush;
    String apiKey;
}
