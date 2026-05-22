package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.dto.TopUpInitiation;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.BankAccount;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Transaction;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.TransactionType;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.BankAccountRepository;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.TransactionRepository;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletFundingServiceTest {

    @Mock
    private WalletAccountService walletAccountService;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private BankAccountRepository bankAccountRepository;

    @Mock
    private WalletEventPublisher eventPublisher;

    @InjectMocks
    private WalletFundingService walletFundingService;

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
    void testInitiateTopUp() {
        TopUpInitiation result = walletFundingService.initiateTopUp(1L, 500L);
        assertEquals("PENDING", result.getStatus());
        assertEquals("00001", result.getVirtualAccountNumber());
        assertEquals("00001", result.getVirtualAccount());
        assertNotNull(result.getPaymentReference());
        assertEquals(500L, result.getAmountToPay());
    }

    @Test
    void testTopUp_Success() {
        when(walletAccountService.getBankAccountByUserId(1L)).thenReturn(mockBankAccount);
        when(walletAccountService.getWalletByUserIdForUpdate(1L)).thenReturn(mockWallet);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        Wallet result = walletFundingService.topUp(1L, 500L, "idempotency-key");

        assertEquals(1500L, result.getBalance());
        assertEquals(4500L, mockBankAccount.getBalance());
        verify(bankAccountRepository).save(mockBankAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(eventPublisher, times(1)).publishEvent(anyLong(), any(TransactionType.class), anyLong(), anyString());
    }

    @Test
    void testTopUp_BankAccountNotFound_ShouldThrowInsufficientBalance() {
        BankAccount emptyBankAccount = BankAccount.builder().userId(1L).balance(0L).build();
        when(walletAccountService.getBankAccountByUserId(1L)).thenReturn(emptyBankAccount);

        assertThrows(IllegalArgumentException.class, () -> walletFundingService.topUp(1L, 500L, "idempotency-key"));
    }

    @Test
    void testTopUp_InsufficientBankBalance() {
        mockBankAccount.setBalance(100L);
        when(walletAccountService.getBankAccountByUserId(1L)).thenReturn(mockBankAccount);

        assertThrows(IllegalArgumentException.class, () -> walletFundingService.topUp(1L, 500L, "idempotency-key"));
    }

    @Test
    void testTopUp_InvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletFundingService.topUp(1L, 0L, "idempotency-key"));
        assertThrows(IllegalArgumentException.class, () -> walletFundingService.topUp(1L, -100L, "idempotency-key"));
    }

    @Test
    void testTopUp_InvalidIdempotencyKey() {
        assertThrows(IllegalArgumentException.class, () -> walletFundingService.topUp(1L, 500L, null));
        assertThrows(IllegalArgumentException.class, () -> walletFundingService.topUp(1L, 500L, "   "));
        assertThrows(IllegalArgumentException.class, () -> walletFundingService.topUp(1L, 500L, ""));
    }

    @Test
    void testTopUp_IdempotencyHit() {
        when(transactionRepository.existsByIdempotencyKey("existing-key")).thenReturn(true);
        when(walletAccountService.getWalletByUserId(1L)).thenReturn(mockWallet);

        Wallet result = walletFundingService.topUp(1L, 500L, "existing-key");

        assertEquals(1000L, result.getBalance());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testWithdraw_Success() {
        when(walletAccountService.getBankAccountByUserId(1L)).thenReturn(mockBankAccount);
        when(walletAccountService.getWalletByUserIdForUpdate(1L)).thenReturn(mockWallet);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        Wallet result = walletFundingService.withdraw(1L, 400L, "idempotency-key");

        assertEquals(600L, result.getBalance());
        assertEquals(5400L, mockBankAccount.getBalance());
        verify(bankAccountRepository).save(mockBankAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(eventPublisher, times(1)).publishEvent(anyLong(), any(TransactionType.class), anyLong(), anyString());
    }

    @Test
    void testWithdraw_InsufficientBalance() {
        when(walletAccountService.getWalletByUserIdForUpdate(1L)).thenReturn(mockWallet);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            walletFundingService.withdraw(1L, 2000L, "idempotency-key");
        });

        assertEquals("Saldo wallet tidak mencukupi untuk melakukan withdraw!", exception.getMessage());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testWithdraw_BankAccountNotFound_ShouldCreateAutomaticallyAndSucceed() {
        BankAccount emptyBankAccount = BankAccount.builder().userId(1L).balance(0L).build();
        when(walletAccountService.getBankAccountByUserId(1L)).thenReturn(emptyBankAccount);
        when(walletAccountService.getWalletByUserIdForUpdate(1L)).thenReturn(mockWallet);
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        Wallet result = walletFundingService.withdraw(1L, 400L, "idempotency-key");

        assertEquals(600L, result.getBalance());
        assertEquals(400L, emptyBankAccount.getBalance());
        verify(bankAccountRepository, times(1)).save(emptyBankAccount);
        verify(transactionRepository, times(1)).save(any(Transaction.class));
    }

    @Test
    void testWithdraw_InvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletFundingService.withdraw(1L, 0L, "key"));
        assertThrows(IllegalArgumentException.class, () -> walletFundingService.withdraw(1L, -50L, "key"));
    }

    @Test
    void testWithdraw_InvalidIdempotencyKey() {
        assertThrows(IllegalArgumentException.class, () -> walletFundingService.withdraw(1L, 500L, null));
        assertThrows(IllegalArgumentException.class, () -> walletFundingService.withdraw(1L, 500L, "   "));
    }

    @Test
    void testWithdraw_IdempotencyHit() {
        when(transactionRepository.existsByIdempotencyKey("existing-key")).thenReturn(true);
        when(walletAccountService.getWalletByUserId(1L)).thenReturn(mockWallet);

        Wallet result = walletFundingService.withdraw(1L, 200L, "existing-key");

        assertEquals(1000L, result.getBalance());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
