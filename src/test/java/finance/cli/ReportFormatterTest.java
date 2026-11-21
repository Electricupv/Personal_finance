package finance.cli;

import finance.model.Wallet;
import finance.service.FinanceService;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportFormatterTest {
    private final FinanceService finance = new FinanceService();

    @Test
    void reportContainsTotalsAndRemainingBudget() {
        Wallet wallet = new Wallet();
        finance.addCategory(wallet, "Зарплата");
        finance.addCategory(wallet, "Еда");
        finance.addIncome(wallet, "Зарплата", new BigDecimal("20000"));
        finance.addExpense(wallet, "Еда", new BigDecimal("300"));
        finance.setBudget(wallet, "Еда", new BigDecimal("1000"));

        String report = new ReportFormatter(finance).fullReport(wallet);

        assertTrue(report.contains("Общий доход: 20,000.0"));
        assertTrue(report.contains("Общие расходы: 300.0"));
        assertTrue(report.contains("Еда: 1000.0, Оставшийся бюджет: 700.0"));
    }
}