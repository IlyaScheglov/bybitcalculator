package sry.mail.BybitCalculator.dto;

import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.math.BigDecimal;

@Value
@Builder
@Jacksonized
public class ChangeUserSettingsDto {

    String tgId;
    BigDecimal minPercentOfDump;
    BigDecimal minPercentOfIncome;
}
