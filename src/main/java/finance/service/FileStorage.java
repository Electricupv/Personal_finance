package finance.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import finance.model.User;
import finance.model.Wallet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FileStorage {
    private final ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    private final Path usersFile;
    private final Path walletsDir;

    public FileStorage(Path dataDir) {
        this.usersFile = dataDir.resolve("users.json");
        this.walletsDir = dataDir.resolve("wallets");
    }

    public Map<String, User> loadUsers() {
        if (Files.notExists(usersFile)) {
            return new HashMap<>();
        }
        return read(usersFile, new TypeReference<Map<String, User>>() {});
    }

    public void saveUsers(Map<String, User> users) {
        write(usersFile, users);
    }

    public Wallet loadWallet(String login) {
        Path file = walletsDir.resolve(login + ".json");
        if (Files.notExists(file)) {
            return new Wallet();
        }
        return read(file, new TypeReference<Wallet>() {});
    }

    public void saveWallet(String login, Wallet wallet) {
        write(walletsDir.resolve(login + ".json"), wallet);
    }

    private <T> T read(Path file, TypeReference<T> type) {
        try {
            return mapper.readValue(file.toFile(), type);
        } catch (IOException e) {
            throw new FinanceException("Не удалось прочитать файл " + file, e);
        }
    }

    private void write(Path file, Object value) {
        try {
            Files.createDirectories(file.toAbsolutePath().getParent());
            mapper.writeValue(file.toFile(), value);
        } catch (IOException e) {
            throw new FinanceException("Не удалось записать файл " + file, e);
        }
    }
}
