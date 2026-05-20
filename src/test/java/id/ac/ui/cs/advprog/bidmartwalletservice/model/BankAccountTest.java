package id.ac.ui.cs.advprog.bidmartwalletservice.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class BankAccountTest {

    @Test
    void testBankAccountBuilderAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        BankAccount bankAccount = BankAccount.builder()
                .userId(1L)
                .bankName("GoPay")
                .accountNumber("123456789")
                .balance(5000L)
                .createdAt(now)
                .build();

        assertEquals(1L, bankAccount.getUserId());
        assertEquals("GoPay", bankAccount.getBankName());
        assertEquals("123456789", bankAccount.getAccountNumber());
        assertEquals(5000L, bankAccount.getBalance());
        assertEquals(now, bankAccount.getCreatedAt());
    }

    @Test
    void testBankAccountSetters() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.setUserId(10L);
        bankAccount.setBankName("BCA");
        bankAccount.setAccountNumber("987654321");
        bankAccount.setBalance(2000L);
        LocalDateTime now = LocalDateTime.now();
        bankAccount.setCreatedAt(now);

        assertEquals(10L, bankAccount.getUserId());
        assertEquals("BCA", bankAccount.getBankName());
        assertEquals("987654321", bankAccount.getAccountNumber());
        assertEquals(2000L, bankAccount.getBalance());
        assertEquals(now, bankAccount.getCreatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        BankAccount bankAccount = new BankAccount();
        assertNotNull(bankAccount);
        assertNull(bankAccount.getUserId());
        assertNull(bankAccount.getBankName());
        assertNull(bankAccount.getAccountNumber());
        assertNull(bankAccount.getBalance());
        assertNull(bankAccount.getCreatedAt());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        BankAccount bankAccount = new BankAccount(1L, "Mandiri", "111222", 1500L, now);
        assertEquals(1L, bankAccount.getUserId());
        assertEquals("Mandiri", bankAccount.getBankName());
        assertEquals("111222", bankAccount.getAccountNumber());
        assertEquals(1500L, bankAccount.getBalance());
        assertEquals(now, bankAccount.getCreatedAt());
    }

    @Test
    void testOnCreate_SetsCreatedAtAndDefaultValues() {
        BankAccount bankAccount = new BankAccount();
        bankAccount.onCreate();
        assertNotNull(bankAccount.getCreatedAt());
        assertEquals(0L, bankAccount.getBalance(), "Balance harus default ke 0L jika null");
    }

    @Test
    void testOnCreate_DoesNotOverrideExistingValues() {
        BankAccount bankAccount = BankAccount.builder()
                .balance(100L)
                .build();
        bankAccount.onCreate();
        assertEquals(100L, bankAccount.getBalance(), "Balance tidak boleh berubah jika sudah ada nilainya");
    }
}
