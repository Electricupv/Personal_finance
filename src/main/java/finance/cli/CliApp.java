package finance.cli;

import finance.model.TransactionType;
import finance.model.Wallet;
import finance.service.AuthService;
import finance.service.BudgetStatus;
import finance.service.FinanceException;
import finance.service.FinanceService;
import finance.service.Money;
import finance.service.Validator;

import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CliApp {
    private static final Pattern TOKEN = Pattern.compile("\"([^\"]*)\"|(\\S+)");

    private final Scanner in;
    private final PrintStream out;
    private final AuthService auth;
    private final FinanceService finance;
    private final ReportFormatter formatter;
    private boolean running = true;

    public CliApp(Scanner in, PrintStream out, AuthService auth, FinanceService finance) {
        this.in = in;
        this.out = out;
        this.auth = auth;
        this.finance = finance;
        this.formatter = new ReportFormatter(finance);
    }

    public void run() {
        out.println("Личные финансы. Для выхода введите exit.");
        while (running) {
            out.print("> ");
            if (!in.hasNextLine()) {
                exit();
                break;
            }
            List<String> tokens = tokenize(in.nextLine());
            if (tokens.isEmpty()) {
                continue;
            }
            try {
                execute(tokens.get(0).toLowerCase(Locale.ROOT), tokens.subList(1, tokens.size()));
            } catch (FinanceException e) {
                out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void execute(String command, List<String> args) {
        switch (command) {
            case "register" -> register(args);
            case "login" -> login(args);
            case "income" -> addIncome(args);
            case "expense" -> addExpense(args);
            case "category" -> addCategory(args);
            case "budget" -> setBudget(args);
            case "report" -> report(args);
            case "stats" -> statistics(args);
            case "exit" -> exit();
            default -> out.println("Неизвестная команда: " + command);
        }
    }

    private void register(List<String> args) {
        requireArgs(args, 2, "register <логин> <пароль>");
        auth.register(args.get(0), args.get(1));
        out.println("Пользователь зарегистрирован. Выполните login.");
    }

    private void login(List<String> args) {
        requireArgs(args, 2, "login <логин> <пароль>");
        out.println("Вы вошли как " + auth.login(args.get(0), args.get(1)).getLogin());
    }

    private void addIncome(List<String> args) {
        Wallet wallet = auth.requireWallet();
        requireArgs(args, 2, "income <категория> <сумма>");
        BigDecimal amount = Validator.parseAmount(args.get(1));
        finance.addIncome(wallet, args.get(0), amount);
        out.println("Доход добавлен: " + args.get(0) + " " + Money.format(amount));
    }

    private void addExpense(List<String> args) {
        Wallet wallet = auth.requireWallet();
        requireArgs(args, 2, "expense <категория> <сумма>");
        BigDecimal amount = Validator.parseAmount(args.get(1));
        List<String> alerts = finance.addExpense(wallet, args.get(0), amount);
        out.println("Расход добавлен: " + args.get(0) + " " + Money.format(amount));
        for (String alert : alerts) {
            out.println("ВНИМАНИЕ: " + alert);
        }
    }

    private void addCategory(List<String> args) {
        Wallet wallet = auth.requireWallet();
        requireArgs(args, 1, "category <название>");
        out.println("Категория добавлена: " + finance.addCategory(wallet, args.get(0)));
    }

    private void setBudget(List<String> args) {
        Wallet wallet = auth.requireWallet();
        requireArgs(args, 2, "budget <категория> <сумма>");
        BigDecimal limit = Validator.parseAmount(args.get(1));
        String category = finance.setBudget(wallet, args.get(0), limit);
        BudgetStatus status = finance.budgetStatus(wallet, category);
        out.println("Бюджет «" + category + "»: " + Money.plain(status.limit())
                + ", Оставшийся бюджет: " + Money.plain(status.remaining()));
    }

    private void report(List<String> args) {
        String text = formatter.fullReport(auth.requireWallet());
        if (args.isEmpty()) {
            out.print(text);
            return;
        }
        Path file = Path.of(args.get(0));
        try {
            Files.writeString(file, text, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new FinanceException("Не удалось записать отчёт в " + file, e);
        }
        out.println("Отчёт сохранён в " + file.toAbsolutePath());
    }

    private void statistics(List<String> args) {
        Wallet wallet = auth.requireWallet();
        requireArgs(args, 1, "stats <категория1> [категория2 ...]");
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (String name : args) {
            Optional<String> category = wallet.findCategory(name.trim());
            if (category.isEmpty()) {
                out.println("Категория не найдена: " + name);
                continue;
            }
            BigDecimal income = finance.total(wallet, category.get(), TransactionType.INCOME);
            BigDecimal expense = finance.total(wallet, category.get(), TransactionType.EXPENSE);
            out.println(category.get() + ": доходы " + Money.format(income) + ", расходы " + Money.format(expense));
            totalIncome = totalIncome.add(income);
            totalExpense = totalExpense.add(expense);
        }
        out.println("Итого по выбранным категориям: доходы " + Money.format(totalIncome)
                + ", расходы " + Money.format(totalExpense));
    }

    private void exit() {
        running = false;
        if (auth.isLoggedIn()) {
            try {
                auth.logout();
                out.println("Данные кошелька сохранены.");
            } catch (FinanceException e) {
                out.println("Ошибка: " + e.getMessage());
            }
        }
        out.println("До свидания!");
    }

    private static void requireArgs(List<String> args, int count, String usage) {
        if (args.size() < count) {
            throw new FinanceException("Недостаточно аргументов. Формат: " + usage);
        }
    }

    private static List<String> tokenize(String line) {
        List<String> tokens = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(line);
        while (matcher.find()) {
            tokens.add(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
        }
        return tokens;
    }
}
