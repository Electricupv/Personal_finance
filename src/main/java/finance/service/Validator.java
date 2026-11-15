package finance.service;

import java.math.BigDecimal;

public final class Validator {

    private Validator() {
    }

    public static String requireNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new FinanceException("Поле «" + fieldName + "» не может быть пустым");
        }
        return value.trim();
    }

    public static BigDecimal parseAmount(String raw) {
        String value = requireNotBlank(raw, "Сумма");
        BigDecimal amount;
        try {
            amount = new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new FinanceException("Некорректная сумма: " + raw);
        }
        requirePositive(amount);
        return amount;
    }

    public static void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new FinanceException("Сумма должна быть больше нуля");
        }
    }
}
