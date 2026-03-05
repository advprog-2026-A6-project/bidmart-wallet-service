package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private WalletService walletService;

    private Wallet mockWallet;

    @BeforeEach
    void setUp() {
        mockWallet = Wallet.builder()
                .userId(1L)
                .balance(1000L)
                .version(0L)
                .build();
    }

    @Test
    void testGetWalletByUserId_Found() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));

        Wallet result = walletService.getWalletByUserId(1L);

        assertNotNull(result);
        assertEquals(1L, result.getUserId());
        assertEquals(1000L, result.getBalance());
        verify(walletRepository, times(1)).findById(1L);
    }

    @Test
    void testGetWalletByUserId_NotFound_ShouldCreateNew() {
        when(walletRepository.findById(2L)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        Wallet result = walletService.getWalletByUserId(2L);

        assertNotNull(result);
        assertEquals(2L, result.getUserId());
        assertEquals(0L, result.getBalance());
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void testTopUp_Success() {
        when(walletRepository.findById(1L)).thenReturn(Optional.of(mockWallet));
        when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArguments()[0]);

        Wallet result = walletService.topUp(1L, 500L);

        assertEquals(1500L, result.getBalance());
        verify(walletRepository, times(1)).save(any(Wallet.class));
    }

    @Test
    void testTopUp_NegativeAmount_ShouldThrowException() {
        assertThrows(IllegalArgumentException.class, () -> {
            walletService.topUp(1L, -100L);
        });
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}
