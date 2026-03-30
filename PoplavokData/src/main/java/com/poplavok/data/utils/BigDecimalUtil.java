package com.poplavok.data.utils;

import javax.annotation.Nullable;
import java.math.BigDecimal;

public class BigDecimalUtil {
    public static BigDecimal nullToZero(@Nullable BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        } else {
            return amount;
        }
    }

    public static String formatAmount(@Nullable BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        BigDecimal stripped = amount.stripTrailingZeros();
        if (stripped.scale() < 2) {
            // only pad with zeros to strictly ensure at least 2 decimal places, no actual rounding occurs
            stripped = stripped.setScale(2, java.math.RoundingMode.UNNECESSARY);
        }
        return stripped.toPlainString();
    }
}
