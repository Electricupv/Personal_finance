package finance.service;

import finance.model.User;
import finance.model.Wallet;

import java.util.Map;

public class AuthService {
    private final FileStorage storage;
    private final Map<String, User> users;
    private User currentUser;
    private Wallet currentWallet;

    public AuthService(FileStorage storage) {
        this.storage = storage;
        this.users = storage.loadUsers();
    }

    public void register(String login, String password) {
        String name = Validator.requireNotBlank(login, "Логин");
        Validator.requireNotBlank(password, "Пароль");
        if (users.containsKey(name)) {
            throw new FinanceException("Пользователь уже существует: " + name);
        }
        users.put(name, new User(name, password));
        storage.saveUsers(users);
    }

    public User login(String login, String password) {
        if (currentUser != null) {
            throw new FinanceException("Вы уже вошли. Сохраните данные командой exit");
        }
        Validator.requireNotBlank(login, "Логин");
        Validator.requireNotBlank(password, "Пароль");
        User user = users.get(login.trim());
        if (user == null || !user.getPassword().equals(password)) {
            throw new FinanceException("Неверный логин или пароль");
        }
        currentUser = user;
        currentWallet = storage.loadWallet(user.getLogin());
        return user;
    }

    public void logout() {
        if (currentUser != null) {
            storage.saveWallet(currentUser.getLogin(), currentWallet);
        }
        currentUser = null;
        currentWallet = null;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public Wallet requireWallet() {
        if (currentUser == null) {
            throw new FinanceException("Сначала выполните login");
        }
        return currentWallet;
    }
}
