package finance.service;

import finance.model.Transaction;
import finance.model.TransactionType;
import finance.model.Wallet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FinanceService {

    public void addIncome(Wallet wallet, String category, BigDecimal amount) {
        add(wallet, TransactionType.INCOME, category, amount);
    }

    public List<String> addExpense(Wallet wallet, String category, BigDecimal amount) {
        String resolved = add(wallet, TransactionType.EXPENSE, category, amount);
        List<String> alerts = new ArrayList<>();
        BigDecimal limit = wallet.getBudgets().get(resolved);
        if (limit != null) {
            BigDecimal spent = total(wallet, resolved, TransactionType.EXPENSE);
            if (spent.compareTo(limit) > 0) {
                alerts.add("Превышен бюджет «" + resolved + "» на " + Money.format(spent.subtract(limit)));
            }
        }
        if (wallet.getBalance().signum() < 0) {
            alerts.add("Расходы превысили доходы на " + Money.format(wallet.getBalance().negate()));
        }
        return alerts;
    }

    public String addCategory(Wallet wallet, String name) {
        String category = Validator.requireNotBlank(name, "Категория");
        if (wallet.findCategory(category).isPresent()) {
            throw new FinanceException("Категория уже существует: " + category);
        }
        wallet.addCategory(category);
        return category;
    }

    public String setBudget(Wallet wallet, String category, BigDecimal limit) {
        Validator.requirePositive(limit);
        String resolved = requireCategory(wallet, category);
        wallet.setBudget(resolved, limit);
        return resolved;
    }

    public BigDecimal total(Wallet wallet, TransactionType type) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Transaction t : wallet.getTransactions()) {
            if (t.type() == type) {
                sum = sum.add(t.amount());
            }
        }
        return sum;
    }

    public BigDecimal total(Wallet wallet, String category, TransactionType type) {
        BigDecimal sum = BigDecimal.ZERO;
        for (Transaction t : wallet.getTransactions()) {
            if (t.type() == type && t.category().equals(category)) {
                sum = sum.add(t.amount());
            }
        }
        return sum;
    }

    public Map<String, BigDecimal> byCategory(Wallet wallet, TransactionType type) {
        Map<String, BigDecimal> result = new LinkedHashMap<>();
        for (Transaction t : wallet.getTransactions()) {
            if (t.type() == type) {
                result.merge(t.category(), t.amount(), BigDecimal::add);
            }
        }
        return result;
    }

    public List<BudgetStatus> budgetStatuses(Wallet wallet) {
        List<BudgetStatus> result = new ArrayList<>();
        wallet.getBudgets().forEach((category, limit) -> result.add(budgetStatus(wallet, category)));
        return result;
    }

    public BudgetStatus budgetStatus(Wallet wallet, String category) {
        BigDecimal limit = wallet.getBudgets().get(category);
        BigDecimal spent = total(wallet, category, TransactionType.EXPENSE);
        return new BudgetStatus(category, limit, spent, limit.subtract(spent));
    }

    private String add(Wallet wallet, TransactionType type, String category, BigDecimal amount) {
        Validator.requirePositive(amount);
        String resolved = requireCategory(wallet, category);
        wallet.addTransaction(new Transaction(type, resolved, amount));
        return resolved;
    }

    private String requireCategory(Wallet wallet, String name) {
        String category = Validator.requireNotBlank(name, "Категория");
        return wallet.findCategory(category)
                .orElseThrow(() -> new FinanceException("Категория не найдена: " + category
                        + ". Сначала создайте её командой category"));
    }
}
