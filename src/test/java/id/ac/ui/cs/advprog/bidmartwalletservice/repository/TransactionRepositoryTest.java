package id.ac.ui.cs.advprog.bidmartwalletservice.repository;

import id.ac.ui.cs.advprog.bidmartwalletservice.model.Transaction;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void testSaveAndFindById() {
        Transaction transaction = Transaction.builder()
                .userId(1L)
                .type("TOPUP")
                .amount(10000L)
                .description("Top up awal")
                .build();

        Transaction saved = transactionRepository.save(transaction);

        assertNotNull(saved.getId());
        Transaction found = transactionRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals(1L, found.getUserId());
        assertEquals("TOPUP", found.getType());
    }

    @Test
    void testFindByUserIdOrderByTimestampDesc() throws InterruptedException {
        Long userId = 1L;
        Long otherUserId = 2L;

        Transaction t1 = Transaction.builder()
                .userId(userId).type("TOPUP").amount(1000L).build();
        transactionRepository.save(t1);

        Thread.sleep(10);

        Transaction t2 = Transaction.builder()
                .userId(userId).type("WITHDRAW").amount(500L).build();
        transactionRepository.save(t2);

        Transaction tOther = Transaction.builder()
                .userId(otherUserId).type("TOPUP").amount(999L).build();
        transactionRepository.save(tOther);

        List<Transaction> history = transactionRepository.findByUserIdOrderByTimestampDesc(userId);

        assertEquals(2, history.size(), "Harusnya hanya mengambil 2 transaksi milik userId 1");

        assertEquals("WITHDRAW", history.get(0).getType(), "Transaksi terbaru (WITHDRAW) harus muncul pertama");
        assertEquals("TOPUP", history.get(1).getType(), "Transaksi lama (TOPUP) harus muncul terakhir");
    }

    @Test
    void testFindByUserIdEmptyResult() {
        List<Transaction> history = transactionRepository.findByUserIdOrderByTimestampDesc(999L);
        assertTrue(history.isEmpty(), "Harusnya kosong jika userId tidak punya riwayat");
    }
}
