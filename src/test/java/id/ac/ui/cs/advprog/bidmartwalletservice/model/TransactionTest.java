package id.ac.ui.cs.advprog.bidmartwalletservice.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void testTransactionBuilderAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        Transaction transaction = Transaction.builder()
                .id(100L)
                .userId(1L)
                .type(TransactionType.WITHDRAW)
                .amount(50000L)
                .description("Transfer ke Bank BCA")
                .createdAt(now)
                .build();

        assertEquals(100L, transaction.getId());
        assertEquals(1L, transaction.getUserId());
        assertEquals(TransactionType.WITHDRAW, transaction.getType());
        assertEquals(50000L, transaction.getAmount());
        assertEquals("Transfer ke Bank BCA", transaction.getDescription());
        assertEquals(now, transaction.getCreatedAt());
    }

    @Test
    void testTransactionSetters() {
        Transaction transaction = new Transaction();
        LocalDateTime now = LocalDateTime.now();

        transaction.setId(200L);
        transaction.setUserId(2L);
        transaction.setType(TransactionType.TOPUP);
        transaction.setAmount(100000L);
        transaction.setDescription("Top up saldo");
        transaction.setCreatedAt(now);

        assertEquals(200L, transaction.getId());
        assertEquals(2L, transaction.getUserId());
        assertEquals(TransactionType.TOPUP, transaction.getType());
        assertEquals(100000L, transaction.getAmount());
        assertEquals("Top up saldo", transaction.getDescription());
        assertEquals(now, transaction.getCreatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        Transaction transaction = new Transaction();
        assertNotNull(transaction);
        assertNull(transaction.getId());
        assertNull(transaction.getUserId());
    }

    @Test
    void testAllArgsConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Transaction transaction = new Transaction(1L, 1L, TransactionType.TOPUP, 1000L, "Desc", 1000L, 0L, "idemp-key", now);

        assertEquals(1L, transaction.getId());
        assertEquals(TransactionType.TOPUP, transaction.getType());
    }

    @Test
    void testOnCreate_SetsTimestamp() {
        Transaction transaction = new Transaction();
        assertNull(transaction.getCreatedAt());

        transaction.onCreate();

        assertNotNull(transaction.getCreatedAt());
        assertTrue(transaction.getCreatedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }
}
