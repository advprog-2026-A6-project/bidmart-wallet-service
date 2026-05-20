package id.ac.ui.cs.advprog.bidmartwalletservice.repository;

import id.ac.ui.cs.advprog.bidmartwalletservice.model.BankAccount;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BankAccountRepositoryTest {

    @Autowired
    private BankAccountRepository bankAccountRepository;

    @Test
    void testSaveAndFindById() {
        BankAccount bankAccount = BankAccount.builder()
                .userId(1L)
                .bankName("BCA")
                .accountNumber("8012341")
                .balance(1000000L)
                .build();

        BankAccount saved = bankAccountRepository.save(bankAccount);

        assertNotNull(saved.getUserId());
        Optional<BankAccount> found = bankAccountRepository.findById(saved.getUserId());

        assertTrue(found.isPresent(), "BankAccount harusnya ditemukan di database");
        assertEquals("BCA", found.get().getBankName());
        assertEquals("8012341", found.get().getAccountNumber());
        assertEquals(1000000L, found.get().getBalance());
    }

    @Test
    void testUpdateBalance() {
        BankAccount bankAccount = BankAccount.builder()
                .userId(2L)
                .bankName("BCA")
                .accountNumber("8012342")
                .balance(1000000L)
                .build();
        bankAccountRepository.save(bankAccount);

        BankAccount savedBank = bankAccountRepository.findById(2L).get();
        savedBank.setBalance(900000L);
        bankAccountRepository.save(savedBank);

        BankAccount updatedBank = bankAccountRepository.findById(2L).get();
        assertEquals(900000L, updatedBank.getBalance(), "Saldo bank harusnya berkurang menjadi 900.000");
    }

    @Test
    void testFindByIdNotFound() {
        Optional<BankAccount> found = bankAccountRepository.findById(999L);
        assertFalse(found.isPresent(), "Harusnya tidak menemukan BankAccount untuk ID yang tidak ada");
    }
}
