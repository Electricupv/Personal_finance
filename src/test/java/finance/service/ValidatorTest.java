package finance.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidatorTest {

    @Test
    void rejectsTextCommaAndNegativeAmount() {
        assertThrows(FinanceException.class, () -> Validator.parseAmount("abc"));
        assertThrows(FinanceException.class, () -> Validator.parseAmount("300,5"));
        assertThrows(FinanceException.class, () -> Validator.parseAmount("-10"));
    }
}
