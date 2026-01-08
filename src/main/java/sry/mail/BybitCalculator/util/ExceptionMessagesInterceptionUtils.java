package sry.mail.BybitCalculator.util;

import lombok.experimental.UtilityClass;

import java.util.function.Supplier;

@UtilityClass
public class ExceptionMessagesInterceptionUtils {

    public String getOrReturnExceptionMessage(Supplier<String> supplier) {
        try {
            return supplier.get();
        } catch (Exception ex) {
            return ex.getMessage();
        }
    }
}
