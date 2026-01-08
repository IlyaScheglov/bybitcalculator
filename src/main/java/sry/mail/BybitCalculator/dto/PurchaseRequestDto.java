package sry.mail.BybitCalculator.dto;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class PurchaseRequestDto {

    String tgId;
    String symbol;
}
