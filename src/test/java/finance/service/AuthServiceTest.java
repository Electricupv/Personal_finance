package finance.service;

import finance.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {
    private final FinanceService finance = new FinanceService();

    @TempDir
    Path dir;

    @Test
    void severalUsersCanLogInAndWrongPasswordIsRejected() {
        AuthService auth = new AuthService(new FileStorage(dir));
        auth.register("ivan", "1234");
        auth.register("petr", "5678");

        assertEquals("ivan", auth.login("ivan", "1234").getLogin());
        auth.logout();
        assertEquals("petr", auth.login("petr", "5678").getLogin());
        auth.logout();
        assertThrows(FinanceException.class, () -> auth.login("ivan", "0000"));
    }

    @Test
    void walletIsSavedOnLogoutAndLoadedOnLogin() {
        AuthService auth = new AuthService(new FileStorage(dir));
        auth.register("ivan", "1234");
        auth.login("ivan", "1234");
        finance.addCategory(auth.requireWallet(), "Еда");
        finance.addExpense(auth.requireWallet(), "Еда", new BigDecimal("300"));
        auth.logout();

        AuthService reopened = new AuthService(new FileStorage(dir));
        reopened.login("ivan", "1234");

        assertEquals(0, new BigDecimal("300").compareTo(
                finance.total(reopened.requireWallet(), "Еда", TransactionType.EXPENSE)));
    }

    @Test
    void rejectsDuplicateRegistration() {
        AuthService auth = new AuthService(new FileStorage(dir));
        auth.register("ivan", "1234");

        assertThrows(FinanceException.class, () -> auth.register("ivan", "9999"));
    }

    @Test
    void rejectsSecondLoginWithoutLogout() {
        AuthService auth = new AuthService(new FileStorage(dir));
        auth.register("ivan", "1234");
        auth.register("petr", "5678");
        auth.login("ivan", "1234");

        assertThrows(FinanceException.class, () -> auth.login("petr", "5678"));
        assertTrue(auth.isLoggedIn());
        assertEquals(0, auth.requireWallet().getTransactions().size());
    }
}
