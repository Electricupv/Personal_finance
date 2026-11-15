package finance.service;

import java.math.BigDecimal;
import java.util.Locale;

public final class Money {

    private Money() {
    }

    public static String format(BigDecimal amount) {
        return String.format(Locale.US, "%," + precision(amount) + "f", amount);
    }

    public static String plain(BigDecimal amount) {
        return String.format(Locale.US, "%" + precision(amount) + "f", amount);
    }

    private static String precision(BigDecimal amount) {
        return amount.stripTrailingZeros().scale() > 1 ? ".2" : ".1";
    }
}
