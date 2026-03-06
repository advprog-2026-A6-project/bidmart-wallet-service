package id.ac.ui.cs.advprog.bidmartwalletservice.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class WalletTest {

    @Test
    void testWalletBuilderAndGetters() {
        LocalDateTime now = LocalDateTime.now();
        Wallet wallet = Wallet.builder()
                .userId(1L)
                .balance(5000L)
                .version(1L)
                .createdAt(now)
                .build();
        assertEquals(1L, wallet.getUserId());
        assertEquals(5000L, wallet.getBalance());
        assertEquals(1L, wallet.getVersion());
        assertEquals(now, wallet.getCreatedAt());
    }

    @Test
    void testWalletSetters() {
        Wallet wallet = new Wallet();
        wallet.setUserId(10L);
        wallet.setBalance(2000L);
        wallet.setVersion(2L);
        LocalDateTime now = LocalDateTime.now();
        wallet.setCreatedAt(now);
        assertEquals(10L, wallet.getUserId());
        assertEquals(2000L, wallet.getBalance());
        assertEquals(2L, wallet.getVersion());
        assertEquals(now, wallet.getCreatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        Wallet wallet = new Wallet();
        assertNotNull(wallet);
        assertNull(wallet.getUserId());
    }

    @Test
    void testOnCreate_SetsCreatedAtAndDefaultBalance() {
        Wallet wallet = new Wallet();
        wallet.onCreate();
        assertNotNull(wallet.getCreatedAt());
        assertEquals(0L, wallet.getBalance(), "Balance harus default ke 0L jika null");
    }

    @Test
    void testOnCreate_DoesNotOverrideExistingBalance() {
        Wallet wallet = Wallet.builder().balance(100L).build();
        wallet.onCreate();
        assertEquals(100L, wallet.getBalance(), "Balance tidak boleh berubah jika sudah ada nilainya");
    }
}
