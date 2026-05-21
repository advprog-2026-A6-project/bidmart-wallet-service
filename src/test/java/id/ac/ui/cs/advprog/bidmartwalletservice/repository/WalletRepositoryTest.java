package id.ac.ui.cs.advprog.bidmartwalletservice.repository;

import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect")
class WalletRepositoryTest {

    @Autowired
    private WalletRepository walletRepository;

    @Test
    void testSaveAndFindById() {
        Wallet wallet = Wallet.builder()
                .userId(100L)
                .balance(5000L)
                .build();
        walletRepository.save(wallet);
        Optional<Wallet> found = walletRepository.findById(100L);
        assertTrue(found.isPresent());
        assertEquals(5000L, found.get().getBalance());
    }

    @Test
    void testUpdateBalance() {
        Wallet wallet = Wallet.builder()
                .userId(200L)
                .balance(1000L)
                .build();
        walletRepository.save(wallet);
        Wallet savedWallet = walletRepository.findById(200L).get();
        savedWallet.setBalance(2000L);
        walletRepository.save(savedWallet);
        assertEquals(2000L, walletRepository.findById(200L).get().getBalance());
    }

    @Test
    void testFindByIdForUpdate() {
        Wallet wallet = Wallet.builder()
                .userId(300L)
                .balance(750L)
                .heldBalance(0L)
                .build();

        walletRepository.save(wallet);

        Optional<Wallet> found = walletRepository.findByIdForUpdate(300L);

        assertTrue(found.isPresent());
        assertEquals(750L, found.get().getBalance());
    }
}
