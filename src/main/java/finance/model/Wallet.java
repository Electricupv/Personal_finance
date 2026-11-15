package finance.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@JsonAutoDetect(fieldVisibility = Visibility.ANY, getterVisibility = Visibility.NONE,
        isGetterVisibility = Visibility.NONE, setterVisibility = Visibility.NONE)
public class Wallet {
    @JsonDeserialize(as = ArrayList.class)
    private List<Transaction> transactions = new ArrayList<>();
    @JsonDeserialize(as = LinkedHashMap.class)
    private Map<String, BigDecimal> budgets = new LinkedHashMap<>();
    @JsonDeserialize(as = LinkedHashSet.class)
    private Set<String> categories = new LinkedHashSet<>();

    public void addTransaction(Transaction transaction) {
        categories.add(transaction.category());
        transactions.add(transaction);
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }

    public Map<String, BigDecimal> getBudgets() {
        return Collections.unmodifiableMap(budgets);
    }

    public void addCategory(String category) {
        categories.add(category);
    }

    public Optional<String> findCategory(String name) {
        return categories.contains(name) ? Optional.of(name) : Optional.empty();
    }

    public void setBudget(String category, BigDecimal limit) {
        categories.add(category);
        budgets.put(category, limit);
    }

    public BigDecimal getBalance() {
        BigDecimal balance = BigDecimal.ZERO;
        for (Transaction t : transactions) {
            if (t.type() == TransactionType.INCOME) {
                balance = balance.add(t.amount());
            } else {
                balance = balance.subtract(t.amount());
            }
        }
        return balance;
    }
}
