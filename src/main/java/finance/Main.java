package finance;

import finance.cli.CliApp;
import finance.service.AuthService;
import finance.service.FileStorage;
import finance.service.FinanceException;
import finance.service.FinanceService;

import java.io.Console;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.Scanner;


public class Main {

    public static void main(String[] args) {
        try {
            AuthService auth = new AuthService(new FileStorage(Path.of("data")));
            Scanner in = new Scanner(System.in, consoleCharset());
            new CliApp(in, System.out, auth, new FinanceService()).run();
        } catch (FinanceException e) {
            System.out.println("Ошибка запуска: " + e.getMessage());
        }
    }

    private static Charset consoleCharset() {
        Console console = System.console();
        return console != null ? console.charset() : Charset.defaultCharset();
    }
}
