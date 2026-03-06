package id.ac.ui.cs.advprog.bidmartwalletservice.initializer;

import id.ac.ui.cs.advprog.bidmartwalletservice.service.WalletService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletInitializerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletInitializer walletInitializer;

    @Test
    void testRun_Success() {
        walletInitializer.run();
        verify(walletService, times(1)).getWalletByUserId(1L);
    }

    @Test
    void testRun_ExceptionHandling() {
        when(walletService.getWalletByUserId(1L)).thenThrow(new RuntimeException("DB Connection Error"));
        walletInitializer.run();
        verify(walletService, times(1)).getWalletByUserId(1L);
    }
}
