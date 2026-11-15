package finance.cli;

import finance.model.TransactionType;
import finance.model.Wallet;
import finance.service.BudgetStatus;
import finance.service.FinanceService;
import finance.service.Money;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Собирает текст полного отчёта. Результат можно вывести в терминал или записать в файл.
 */
public class ReportFormatter {
    private static final String NL = System.lineSeparator();

    private final FinanceService finance;

    public ReportFormatter(FinanceService finance) {
        this.finance = finance;
    }

    public String fullReport(Wallet wallet) {
        StringBuilder sb = new StringBuilder();
        sb.append("Общий доход: ").append(Money.format(finance.total(wallet, TransactionType.INCOME))).append(NL);
        sb.append("Доходы по категориям:").append(NL);
        appendCategories(sb, finance.byCategory(wallet, TransactionType.INCOME));
        sb.append("Общие расходы: ").append(Money.format(finance.total(wallet, TransactionType.EXPENSE))).append(NL);
        sb.append("Расходы по категориям:").append(NL);
        appendCategories(sb, finance.byCategory(wallet, TransactionType.EXPENSE));
        sb.append("Бюджет по категориям:").append(NL);
        if (wallet.getBudgets().isEmpty()) {
            sb.append("Бюджеты не установлены").append(NL);
        }
        for (BudgetStatus s : finance.budgetStatuses(wallet)) {
            sb.append(s.category()).append(": ").append(Money.plain(s.limit()))
                    .append(", Оставшийся бюджет: ").append(Money.plain(s.remaining())).append(NL);
        }
        return sb.toString();
    }

    private void appendCategories(StringBuilder sb, Map<String, BigDecimal> totals) {
        if (totals.isEmpty()) {
            sb.append("Нет данных").append(NL);
        }
        totals.forEach((category, amount) ->
                sb.append(category).append(": ").append(Money.format(amount)).append(NL));
    }
}
