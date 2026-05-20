package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.dto.TopUpInitiation;
import id.ac.ui.cs.advprog.bidmartwalletservice.dto.WalletEvent;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.BankAccount;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Transaction;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.TransactionType;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceTest {

    @Mock
    private WalletAccountService walletAccountService;

    @Mock
    private WalletFundingService walletFundingService;

    @Mock
    private WalletAuctionService walletAuctionService;

    @Mock
    private WalletEventPublisher eventPublisher;

    @InjectMocks
    private WalletService walletService;

    @Test
    void testGetWalletByUserId() {
        Wallet mockWallet = Wallet.builder().userId(1L).build();
        when(walletAccountService.getWalletByUserId(1L)).thenReturn(mockWallet);

        Wallet result = walletService.getWalletByUserId(1L);

        assertEquals(mockWallet, result);
        verify(walletAccountService).getWalletByUserId(1L);
    }

    @Test
    void testGetBankAccountByUserId() {
        BankAccount mockBankAccount = BankAccount.builder().userId(1L).build();
        when(walletAccountService.getBankAccountByUserId(1L)).thenReturn(mockBankAccount);

        BankAccount result = walletService.getBankAccountByUserId(1L);

        assertEquals(mockBankAccount, result);
        verify(walletAccountService).getBankAccountByUserId(1L);
    }

    @Test
    void testInitiateTopUp() {
        TopUpInitiation mockInitiation = TopUpInitiation.builder().build();
        when(walletFundingService.initiateTopUp(1L, 500L)).thenReturn(mockInitiation);

        TopUpInitiation result = walletService.initiateTopUp(1L, 500L);

        assertEquals(mockInitiation, result);
        verify(walletFundingService).initiateTopUp(1L, 500L);
    }

    @Test
    void testTopUp() {
        Wallet mockWallet = Wallet.builder().userId(1L).build();
        when(walletFundingService.topUp(1L, 500L, "key")).thenReturn(mockWallet);

        Wallet result = walletService.topUp(1L, 500L, "key");

        assertEquals(mockWallet, result);
        verify(walletFundingService).topUp(1L, 500L, "key");
    }

    @Test
    void testWithdraw() {
        Wallet mockWallet = Wallet.builder().userId(1L).build();
        when(walletFundingService.withdraw(1L, 500L, "key")).thenReturn(mockWallet);

        Wallet result = walletService.withdraw(1L, 500L, "key");

        assertEquals(mockWallet, result);
        verify(walletFundingService).withdraw(1L, 500L, "key");
    }

    @Test
    void testGetHistory() {
        List<Transaction> mockHistory = new ArrayList<>();
        when(walletAccountService.getHistory(1L)).thenReturn(mockHistory);

        List<Transaction> result = walletService.getHistory(1L);

        assertEquals(mockHistory, result);
        verify(walletAccountService).getHistory(1L);
    }

    @Test
    void testHoldAmount() {
        walletService.holdAmount(1L, 500L, "key");
        verify(walletAuctionService).holdAmount(1L, 500L, "key");
    }

    @Test
    void testReleaseAmount() {
        walletService.releaseAmount(1L, 500L, "key");
        verify(walletAuctionService).releaseAmount(1L, 500L, "key");
    }

    @Test
    void testSettlePayment() {
        walletService.settlePayment(1L, 2L, 500L, "key");
        verify(walletAuctionService).settlePayment(1L, 2L, 500L, "key");
    }

    @Test
    void testHandleWalletEvent() {
        WalletEvent event = WalletEvent.builder()
                .userId(1L)
                .type("TOPUP")
                .amount(500L)
                .description("Desc")
                .build();

        walletService.handleWalletEvent(event);

        verify(eventPublisher).publishEvent(
                1L,
                TransactionType.TOPUP,
                500L,
                "Desc"
        );
    }
}