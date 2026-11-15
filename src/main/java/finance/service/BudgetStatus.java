package finance.service;

import java.math.BigDecimal;

/**
 * Состояние бюджета категории: лимит, потрачено и остаток (может быть отрицательным).
 */
public record BudgetStatus(String category, BigDecimal limit, BigDecimal spent, BigDecimal remaining) {
}
