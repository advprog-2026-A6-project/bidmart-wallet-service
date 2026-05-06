package id.ac.ui.cs.advprog.bidmartwalletservice.controller;

import id.ac.ui.cs.advprog.bidmartwalletservice.model.Transaction;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import id.ac.ui.cs.advprog.bidmartwalletservice.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
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
    void testGetWallet() throws Exception {
        when(walletService.getWalletByUserId(1L)).thenReturn(mockWallet);

        mockMvc.perform(get("/api/wallet/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void testTopUp() throws Exception {
        when(walletService.topUp(1L, 500L)).thenReturn(
                Wallet.builder().userId(1L).balance(1500L).version(1L).build()
        );

        mockMvc.perform(post("/api/wallet/1/topup")
                        .param("amount", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500));
    }

    @Test
    void testWithdraw() throws Exception {
        Wallet updatedWallet = Wallet.builder().userId(1L).balance(800L).version(1L).build();

        when(walletService.withdraw(1L, 200L, "BCA-123")).thenReturn(updatedWallet);

        mockMvc.perform(post("/api/wallet/1/withdraw")
                        .param("amount", "200")
                        .param("bankAccount", "BCA-123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(800));
    }

    @Test
    void testGetHistory() throws Exception {
        Transaction t1 = Transaction.builder()
                .id(1L).userId(1L).type("TOPUP").amount(1000L)
                .description("Top-up").createdAt(LocalDateTime.now()).build();

        Transaction t2 = Transaction.builder()
                .id(2L).userId(1L).type("WITHDRAW").amount(500L)
                .description("Transfer ke Bank").createdAt(LocalDateTime.now()).build();

        List<Transaction> mockHistory = Arrays.asList(t1, t2);

        when(walletService.getHistory(1L)).thenReturn(mockHistory);

        mockMvc.perform(get("/api/wallet/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("TOPUP"))
                .andExpect(jsonPath("$[1].type").value("WITHDRAW"));
    }

    @Test
    void testHoldBalance() throws Exception {
        mockMvc.perform(post("/api/wallet/1/hold")
                        .param("amount", "300"))
                .andExpect(status().isOk())
                .andExpect(content().string("Balance held successfully"));
    }

    @Test
    void testReleaseBalance() throws Exception {
        mockMvc.perform(post("/api/wallet/1/release")
                        .param("amount", "300"))
                .andExpect(status().isOk())
                .andExpect(content().string("Balance released successfully"));
    }

    @Test
    void testSettlePayment() throws Exception {
        mockMvc.perform(post("/api/wallet/1/settle")
                        .param("amount", "300"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment settled successfully"));
    }
}