package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.model.Transaction;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.TransactionType;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.TransactionRepository;
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
class WalletAuctionServiceTest {

    @Mock
    private WalletAccountService walletAccountService;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletEventPublisher eventPublisher;

    @InjectMocks
    private WalletAuctionService walletAuctionService;

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
    void testHoldAmount_Success() {
        when(walletAccountService.getWalletByUserIdForUpdate(1L)).thenReturn(mockWallet);
        walletAuctionService.holdAmount(1L, 400L, "idempotency-key");

        assertEquals(600L, mockWallet.getBalance());
        assertEquals(400L, mockWallet.getHeldBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(eventPublisher).publishEvent(1L, TransactionType.HOLD, 400L, "Dana ditahan untuk penawaran lelang");
    }

    @Test
    void testHoldAmount_InvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletAuctionService.holdAmount(1L, 0L, "key"));
        assertThrows(IllegalArgumentException.class, () -> walletAuctionService.holdAmount(1L, -100L, "key"));
    }

    @Test
    void testHoldAmount_InvalidIdempotencyKey() {
        assertThrows(IllegalArgumentException.class, () -> walletAuctionService.holdAmount(1L, 100L, null));
        assertThrows(IllegalArgumentException.class, () -> walletAuctionService.holdAmount(1L, 100L, "   "));
        assertThrows(IllegalArgumentException.class, () -> walletAuctionService.holdAmount(1L, 100L, ""));
    }

    @Test
    void testHoldAmount_IdempotencyHit() {
        when(transactionRepository.existsByIdempotencyKey("existing-key")).thenReturn(true);

        walletAuctionService.holdAmount(1L, 400L, "existing-key");

        verify(walletAccountService, never()).getWalletByUserIdForUpdate(1L);
    }

    @Test
    void testHoldAmount_InsufficientBalance() {
        when(walletAccountService.getWalletByUserIdForUpdate(1L)).thenReturn(mockWallet);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            walletAuctionService.holdAmount(1L, 2000L, "idempotency-key");
        });

        assertEquals("Saldo tidak cukup untuk melakukan bid", exception.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testReleaseAmount_Success() {
        mockWallet.setBalance(700L);
        mockWallet.setHeldBalance(300L);

        when(walletAccountService.getWalletByUserIdForUpdate(1L)).thenReturn(mockWallet);
        walletAuctionService.releaseAmount(1L, 300L, "idempotency-key");

        assertEquals(1000L, mockWallet.getBalance());
        assertEquals(0L, mockWallet.getHeldBalance());
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(eventPublisher).publishEvent(1L, TransactionType.RELEASE, 300L, "Dana dilepaskan karena bid kalah");
    }

    @Test
    void testReleaseAmount_InvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletAuctionService.releaseAmount(1L, 0L, "key"));
        assertThrows(IllegalArgumentException.class, () -> walletAuctionService.releaseAmount(1L, -50L, "key"));
    }

    @Test
    void testReleaseAmount_InvalidIdempotencyKey() {
        assertThrows(IllegalArgumentException.class, () -> walletAuctionService.releaseAmount(1L, 100L, null));
        assertThrows(IllegalArgumentException.class, () -> walletAuctionService.releaseAmount(1L, 100L, "   "));
    }

    @Test
    void testReleaseAmount_IdempotencyHit() {
        when(transactionRepository.existsByIdempotencyKey("existing-key")).thenReturn(true);

        walletAuctionService.releaseAmount(1L, 100L, "existing-key");

        verify(walletAccountService, never()).getWalletByUserIdForUpdate(1L);
    }

    @Test
    void testReleaseAmount_InconsistentData() {
        mockWallet.setBalance(700L);
        mockWallet.setHeldBalance(100L);
        when(walletAccountService.getWalletByUserIdForUpdate(1L)).thenReturn(mockWallet);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            walletAuctionService.releaseAmount(1L, 300L, "idempotency-key");
        });

        assertEquals("Data held balance tidak konsisten untuk release!", exception.getMessage());
    }

    @Test
    void testSettlePayment_Success() {
        mockWallet.setBalance(700L);
        mockWallet.setHeldBalance(300L);

        Wallet sellerWallet = Wallet.builder().userId(2L).balance(0L).heldBalance(0L).build();

        when(walletAccountService.getWalletByUserIdForUpdate(1L)).thenReturn(mockWallet);
        when(walletAccountService.getWalletByUserIdForUpdate(2L)).thenReturn(sellerWallet);
        walletAuctionService.settlePayment(1L, 2L, 300L, "idempotency-key");

        assertEquals(700L, mockWallet.getBalance());
        assertEquals(0L, mockWallet.getHeldBalance());
        assertEquals(300L, sellerWallet.getBalance());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
        verify(eventPublisher).publishEvent(1L, TransactionType.PAYMENT, 300L, "Pembayaran lelang dimenangkan");
        verify(eventPublisher).publishEvent(2L, TransactionType.RECEIPT, 300L, "Penerimaan dana dari hasil lelang");
    }

    @Test
    void testSettlePayment_InvalidAmount() {
        assertThrows(IllegalArgumentException.class, () -> walletAuctionService.settlePayment(1L, 2L, 0L, "key"));
        assertThrows(IllegalArgumentException.class, () -> walletAuctionService.settlePayment(1L, 2L, -100L, "key"));
    }

    @Test
    void testSettlePayment_InvalidIdempotencyKey() {
        assertThrows(IllegalArgumentException.class, () -> walletAuctionService.settlePayment(1L, 2L, 100L, null));
        assertThrows(IllegalArgumentException.class, () -> walletAuctionService.settlePayment(1L, 2L, 100L, "   "));
    }

    @Test
    void testSettlePayment_IdempotencyHit() {
        when(transactionRepository.existsByIdempotencyKey("existing-key")).thenReturn(true);

        walletAuctionService.settlePayment(1L, 2L, 100L, "existing-key");

        verify(walletAccountService, never()).getWalletByUserIdForUpdate(anyLong());
    }

    @Test
    void testSettlePayment_InconsistentData() {
        mockWallet.setHeldBalance(100L);
        when(walletAccountService.getWalletByUserIdForUpdate(1L)).thenReturn(mockWallet);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            walletAuctionService.settlePayment(1L, 2L, 500L, "idempotency-key");
        });

        assertEquals("Data held balance pembeli tidak konsisten!", exception.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }
}
