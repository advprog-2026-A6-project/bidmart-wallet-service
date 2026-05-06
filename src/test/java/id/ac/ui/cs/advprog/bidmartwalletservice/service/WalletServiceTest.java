package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.model.Transaction;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
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
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository; // Mock baru

    @InjectMocks
    private WalletService walletService;

    private Wallet mockWallet;

    @BeforeEach
    void setUp() {
        mockWallet = Wallet.builder()
                .userId(1L)
                .balance(1000L)
                .heldBalance(0L)
                .version(0L)
                .build();
    }

    @Test
    void testGetWalletByUserId_Found() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));
        Wallet result = walletService.getWalletByUserId(1L);
        assertEquals(1000L, result.getBalance());
    }

    @Test
    void testGetWalletByUserId_NotFound_ShouldCreateNew() {
        when(walletRepository.findById(2L)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        Wallet result = walletService.getWalletByUserId(2L);
        assertEquals(0L, result.getBalance());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void testTopUp_Success() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        Wallet result = walletService.topUp(1L, 500L);

        assertEquals(1500L, result.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testTopUp_InvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletService.topUp(1L, 0L));
        assertThrows(IllegalArgumentException.class, () -> walletService.topUp(1L, -100L));
    }

    @Test
    void testWithdraw_Success() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        Wallet result = walletService.withdraw(1L, 400L, "BCA-123");

        assertEquals(600L, result.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testWithdraw_InsufficientBalance() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            walletService.withdraw(1L, 2000L, "BCA-123");
        });

        assertEquals("Saldo tidak mencukupi untuk penarikan", exception.getMessage());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testWithdraw_InvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletService.withdraw(1L, -50L, "BCA-123"));
    }

    @Test
    void testWithdraw_EmptyBankAccount() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));

        assertThrows(IllegalArgumentException.class, () -> walletService.withdraw(1L, 100L, ""));
        assertThrows(IllegalArgumentException.class, () -> walletService.withdraw(1L, 100L, null));
    }

    @Test
    void testGetHistory() {
        List<Transaction> mockHistory = Arrays.asList(new Transaction(), new Transaction());
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(1L)).thenReturn(mockHistory);

        List<Transaction> result = walletService.getHistory(1L);

        assertEquals(2, result.size());
        verify(transactionRepository).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void testHoldAmount_Success() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        walletService.holdAmount(1L, 400L);

        assertEquals(600L, mockWallet.getBalance());
        assertEquals(400L, mockWallet.getHeldBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(walletRepository).save(mockWallet);
    }

    @Test
    void testHoldAmount_InsufficientBalance() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            walletService.holdAmount(1L, 2000L);
        });

        assertEquals("Saldo tidak cukup untuk melakukan bid", exception.getMessage());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testReleaseAmount_Success() {
        mockWallet.setBalance(700L);
        mockWallet.setHeldBalance(300L);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        walletService.releaseAmount(1L, 300L);

        assertEquals(1000L, mockWallet.getBalance());
        assertEquals(0L, mockWallet.getHeldBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(walletRepository).save(mockWallet);
    }

    @Test
    void testSettlePayment_Success() {
        mockWallet.setBalance(700L);
        mockWallet.setHeldBalance(300L);

        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        walletService.settlePayment(1L, 300L);

        assertEquals(700L, mockWallet.getBalance());
        assertEquals(0L, mockWallet.getHeldBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(walletRepository).save(mockWallet);
    }

    @Test
    void testSettlePayment_InconsistentData() {
        mockWallet.setHeldBalance(100L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            walletService.settlePayment(1L, 500L);
        });

        assertEquals("Data held balance tidak konsisten!", exception.getMessage());
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}