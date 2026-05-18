package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.config.RabbitMQConfig;
import id.ac.ui.cs.advprog.bidmartwalletservice.dto.WalletEvent;
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

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;

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
    private TransactionRepository transactionRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WalletService walletService;

    private Wallet mockWallet;
    private Transaction mockTransaction;

    @BeforeEach
    void setUp() {
        mockWallet = Wallet.builder()
                .userId(1L)
                .balance(1000L)
                .heldBalance(0L)
                .version(0L)
                .build();

        mockTransaction = new Transaction();
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

        Wallet result = walletService.topUp(1L, 500L, "idempotency-key");

        assertEquals(1500L, result.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(eventPublisher, times(1)).publishEvent(any(WalletEvent.class));
    }

    @Test
    void testTopUp_InvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletService.topUp(1L, 0L, "idempotency-key"));
        assertThrows(IllegalArgumentException.class, () -> walletService.topUp(1L, -100L, "idempotency-key"));
    }

    @Test
    void testTopUp_InvalidIdempotencyKey() {
        assertThrows(IllegalArgumentException.class, () -> walletService.topUp(1L, 500L, null));
        assertThrows(IllegalArgumentException.class, () -> walletService.topUp(1L, 500L, "   "));
        assertThrows(IllegalArgumentException.class, () -> walletService.topUp(1L, 500L, ""));
    }

    @Test
    void testTopUp_IdempotencyHit() {
        when(transactionRepository.findByIdempotencyKey("existing-key")).thenReturn(Optional.of(mockTransaction));
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));

        Wallet result = walletService.topUp(1L, 500L, "existing-key");

        assertEquals(1000L, result.getBalance());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }


    @Test
    void testWithdraw_Success() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        Wallet result = walletService.withdraw(1L, 400L, "BCA-123", "idempotency-key");

        assertEquals(600L, result.getBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(eventPublisher, times(1)).publishEvent(any(WalletEvent.class));
    }

    @Test
    void testWithdraw_InsufficientBalance() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            walletService.withdraw(1L, 2000L, "BCA-123", "idempotency-key");
        });

        assertEquals("Saldo tidak mencukupi untuk penarikan", exception.getMessage());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testWithdraw_InvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletService.withdraw(1L, 0L, "BCA-123", "key"));
        assertThrows(IllegalArgumentException.class, () -> walletService.withdraw(1L, -50L, "BCA-123", "key"));
    }

    @Test
    void testWithdraw_EmptyBankAccount() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));

        assertThrows(IllegalArgumentException.class, () -> walletService.withdraw(1L, 100L, "", "idempotency-key"));
        assertThrows(IllegalArgumentException.class, () -> walletService.withdraw(1L, 100L, null, "idempotency-key"));
    }

    @Test
    void testWithdraw_InvalidIdempotencyKey() {
        assertThrows(IllegalArgumentException.class, () -> walletService.withdraw(1L, 500L, "BCA-123", null));
        assertThrows(IllegalArgumentException.class, () -> walletService.withdraw(1L, 500L, "BCA-123", "   "));
    }

    @Test
    void testWithdraw_IdempotencyHit() {
        when(transactionRepository.findByIdempotencyKey("existing-key")).thenReturn(Optional.of(mockTransaction));
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));

        Wallet result = walletService.withdraw(1L, 200L, "BCA-123", "existing-key");

        assertEquals(1000L, result.getBalance());
        verify(transactionRepository, never()).save(any(Transaction.class));
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

        walletService.holdAmount(1L, 400L, "idempotency-key");

        assertEquals(600L, mockWallet.getBalance());
        assertEquals(400L, mockWallet.getHeldBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(walletRepository).save(mockWallet);
    }

    @Test
    void testHoldAmount_InvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletService.holdAmount(1L, 0L, "key"));
        assertThrows(IllegalArgumentException.class, () -> walletService.holdAmount(1L, -100L, "key"));
    }

    @Test
    void testHoldAmount_InvalidIdempotencyKey() {
        assertThrows(IllegalArgumentException.class, () -> walletService.holdAmount(1L, 100L, null));
        assertThrows(IllegalArgumentException.class, () -> walletService.holdAmount(1L, 100L, "   "));
        assertThrows(IllegalArgumentException.class, () -> walletService.holdAmount(1L, 100L, ""));
    }

    @Test
    void testHoldAmount_IdempotencyHit() {
        when(transactionRepository.findByIdempotencyKey("existing-key")).thenReturn(Optional.of(mockTransaction));

        walletService.holdAmount(1L, 400L, "existing-key");

        verify(walletRepository, never()).findById(1L);
    }

    @Test
    void testHoldAmount_InsufficientBalance() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            walletService.holdAmount(1L, 2000L, "idempotency-key");
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

        walletService.releaseAmount(1L, 300L, "idempotency-key");

        assertEquals(1000L, mockWallet.getBalance());
        assertEquals(0L, mockWallet.getHeldBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(walletRepository).save(mockWallet);
    }

    @Test
    void testReleaseAmount_InvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletService.releaseAmount(1L, 0L, "key"));
        assertThrows(IllegalArgumentException.class, () -> walletService.releaseAmount(1L, -50L, "key"));
    }

    @Test
    void testReleaseAmount_InvalidIdempotencyKey() {
        assertThrows(IllegalArgumentException.class, () -> walletService.releaseAmount(1L, 100L, null));
        assertThrows(IllegalArgumentException.class, () -> walletService.releaseAmount(1L, 100L, "   "));
    }

    @Test
    void testReleaseAmount_IdempotencyHit() {
        when(transactionRepository.findByIdempotencyKey("existing-key")).thenReturn(Optional.of(mockTransaction));

        walletService.releaseAmount(1L, 100L, "existing-key");

        verify(walletRepository, never()).findById(1L);
    }

    @Test
    void testReleaseAmount_InconsistentData() {
        mockWallet.setBalance(700L);
        mockWallet.setHeldBalance(100L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            walletService.releaseAmount(1L, 300L, "idempotency-key");
        });

        assertEquals("Data held balance tidak konsisten untuk release!", exception.getMessage());
    }

    @Test
    void testSettlePayment_Success() {
        mockWallet.setBalance(700L);
        mockWallet.setHeldBalance(300L);

        Wallet sellerWallet = Wallet.builder().userId(2L).balance(0L).heldBalance(0L).build();

        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));
        when(walletRepository.findById(2L)).thenReturn(Optional.of(sellerWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        walletService.settlePayment(1L, 2L, 300L, "idempotency-key");

        assertEquals(700L, mockWallet.getBalance());
        assertEquals(0L, mockWallet.getHeldBalance());
        assertEquals(300L, sellerWallet.getBalance());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(walletRepository, times(2)).save(any(Wallet.class));
    }

    @Test
    void testSettlePayment_InvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletService.settlePayment(1L, 2L, 0L, "key"));
        assertThrows(IllegalArgumentException.class, () -> walletService.settlePayment(1L, 2L, -100L, "key"));
    }

    @Test
    void testSettlePayment_InvalidIdempotencyKey() {
        assertThrows(IllegalArgumentException.class, () -> walletService.settlePayment(1L, 2L, 100L, null));
        assertThrows(IllegalArgumentException.class, () -> walletService.settlePayment(1L, 2L, 100L, "   "));
    }

    @Test
    void testSettlePayment_IdempotencyHit() {
        when(transactionRepository.findByIdempotencyKey("existing-key")).thenReturn(Optional.of(mockTransaction));

        walletService.settlePayment(1L, 2L, 100L, "existing-key");

        verify(walletRepository, never()).findById(anyLong());
    }

    @Test
    void testSettlePayment_InconsistentData() {
        mockWallet.setHeldBalance(100L);
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            walletService.settlePayment(1L, 2L, 500L, "idempotency-key");
        });

        assertEquals("Data held balance pembeli tidak konsisten!", exception.getMessage());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testHandleWalletEvent() {
        WalletEvent event = WalletEvent.builder()
                .userId(1L)
                .type("TOPUP")
                .amount(500L)
                .description("Desc")
                .timestamp("2026-05-18T10:00:00")
                .build();

        walletService.handleWalletEvent(event);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.WALLET_EXCHANGE),
                eq("wallet.event.topup"),
                eq(event)
        );
    }
}