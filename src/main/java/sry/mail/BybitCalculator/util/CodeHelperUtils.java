package sry.mail.BybitCalculator.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CodeHelperUtils {

    public static <T> T oldOrNewValue(T oldValue, T newValue) {
        return newValue != null ? newValue : oldValue;
    }
}
