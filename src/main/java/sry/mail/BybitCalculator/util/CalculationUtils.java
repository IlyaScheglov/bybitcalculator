package sry.mail.BybitCalculator.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class CalculationUtils {

    public static BigDecimal calculateDiffInPercents(BigDecimal oldValue, BigDecimal newValue) {
        if (oldValue.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return newValue.subtract(oldValue)
                .divide(oldValue, 100, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(10, RoundingMode.HALF_UP);
    }
}
