package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.model.BankAccount;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Transaction;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.BankAccountRepository;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.TransactionRepository;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletAccountServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @InjectMocks
    private WalletAccountService walletAccountService;

    private Wallet mockWallet;
    private BankAccount mockBankAccount;

    @BeforeEach
    void setUp() {
        mockWallet = Wallet.builder()
                .userId(1L)
                .balance(1000L)
                .heldBalance(0L)
                .version(0L)
                .build();

        mockBankAccount = BankAccount.builder()
                .userId(1L)
                .bankName("BCA")
                .accountNumber("1234567890")
                .balance(5000L)
                .build();
    }

    @Test
    void testGetWalletByUserId_Found() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));
        Wallet result = walletAccountService.getWalletByUserId(1L);
        assertEquals(1000L, result.getBalance());
    }

    @Test
    void testGetWalletByUserId_NotFound_ShouldCreateNew() {
        when(walletRepository.findById(2L)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        Wallet result = walletAccountService.getWalletByUserId(2L);
        assertEquals(0L, result.getBalance());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void testGetBankAccountByUserId_Found() {
        when(bankAccountRepository.findById(1L)).thenReturn(Optional.of(mockBankAccount));
        BankAccount result = walletAccountService.getBankAccountByUserId(1L);
        assertEquals("BCA", result.getBankName());
        assertEquals("1234567890", result.getAccountNumber());
        assertEquals(5000L, result.getBalance());
    }

    @Test
    void testGetBankAccountByUserId_NotFound() {
        when(bankAccountRepository.findById(2L)).thenReturn(Optional.empty());
        when(bankAccountRepository.save(any(BankAccount.class))).thenAnswer(i -> i.getArguments()[0]);

        BankAccount result = walletAccountService.getBankAccountByUserId(2L);
        assertEquals("BCA", result.getBankName());
        assertEquals("8012342", result.getAccountNumber());
        assertEquals(0L, result.getBalance());
        verify(bankAccountRepository).save(any(BankAccount.class));
    }

    @Test
    void testGetHistory() {
        List<Transaction> mockHistory = Arrays.asList(new Transaction(), new Transaction());
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(mockHistory);

        List<Transaction> result = walletAccountService.getHistory(1L);

        assertEquals(2, result.size());
        verify(transactionRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }
}
