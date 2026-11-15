package finance.service;

import finance.model.TransactionType;
import finance.model.Wallet;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FinanceServiceTest {
    private final FinanceService service = new FinanceService();

    @Test
    void totalsAndRemainingBudgetsMatchSpec() {
        Wallet wallet = specWallet();

        assertAmount("63000", service.total(wallet, TransactionType.INCOME));
        assertAmount("8300", service.total(wallet, TransactionType.EXPENSE));
        assertAmount("60000", service.total(wallet, "Зарплата", TransactionType.INCOME));
        assertAmount("3200", service.budgetStatus(wallet, "Еда").remaining());
        assertAmount("0", service.budgetStatus(wallet, "Развлечения").remaining());
        assertAmount("-500", service.budgetStatus(wallet, "Коммунальные услуги").remaining());
    }

    @Test
    void warnsWhenBudgetExceededAndExpensesExceedIncome() {
        Wallet wallet = new Wallet();
        service.addCategory(wallet, "Еда");
        service.addCategory(wallet, "Зарплата");
        service.setBudget(wallet, "Еда", amount("100"));
        service.addIncome(wallet, "Зарплата", amount("50"));

        List<String> alerts = service.addExpense(wallet, "Еда", amount("120"));

        assertTrue(alerts.stream().anyMatch(alert -> alert.startsWith("Превышен бюджет «Еда»")));
        assertTrue(alerts.stream().anyMatch(alert -> alert.startsWith("Расходы превысили доходы")));
    }

    @Test
    void rejectsUnknownCategoryAndNonPositiveAmount() {
        Wallet wallet = new Wallet();

        assertThrows(FinanceException.class, () -> service.addExpense(wallet, "Еда", amount("10")));
        service.addCategory(wallet, "Еда");
        assertThrows(FinanceException.class, () -> service.addExpense(wallet, "Еда", amount("0")));
        assertEquals(0, wallet.getTransactions().size());
    }

    private Wallet specWallet() {
        Wallet wallet = new Wallet();
        for (String category : List.of("Еда", "Развлечения", "Коммунальные услуги", "Такси", "Зарплата", "Бонус")) {
            service.addCategory(wallet, category);
        }
        service.addExpense(wallet, "Еда", amount("300"));
        service.addExpense(wallet, "Еда", amount("500"));
        service.addExpense(wallet, "Развлечения", amount("3000"));
        service.addExpense(wallet, "Коммунальные услуги", amount("3000"));
        service.addExpense(wallet, "Такси", amount("1500"));
        service.addIncome(wallet, "Зарплата", amount("20000"));
        service.addIncome(wallet, "Зарплата", amount("40000"));
        service.addIncome(wallet, "Бонус", amount("3000"));
        service.setBudget(wallet, "Еда", amount("4000"));
        service.setBudget(wallet, "Развлечения", amount("3000"));
        service.setBudget(wallet, "Коммунальные услуги", amount("2500"));
        return wallet;
    }

    private static BigDecimal amount(String value) {
        return new BigDecimal(value);
    }

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, amount(expected).compareTo(actual));
    }
}
