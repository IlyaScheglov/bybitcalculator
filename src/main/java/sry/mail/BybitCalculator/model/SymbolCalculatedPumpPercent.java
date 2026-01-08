package sry.mail.BybitCalculator.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SymbolCalculatedPumpPercent {

    String symbol;
    BigDecimal pumpPercent;
}
