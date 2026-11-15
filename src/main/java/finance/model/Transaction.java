package finance.model;

import java.math.BigDecimal;

public record Transaction(TransactionType type, String category, BigDecimal amount) {
}
