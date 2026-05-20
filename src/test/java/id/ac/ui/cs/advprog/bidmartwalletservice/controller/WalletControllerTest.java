package id.ac.ui.cs.advprog.bidmartwalletservice.controller;

import id.ac.ui.cs.advprog.bidmartwalletservice.model.BankAccount;
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

        mockMvc.perform(get("/api/wallet").header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.balance").value(1000));
    }

    @Test
    void testGetBankAccount() throws Exception {
        BankAccount mockBankAccount = BankAccount.builder()
                .userId(1L)
                .bankName("BCA")
                .accountNumber("8012341")
                .balance(5000L)
                .build();
        when(walletService.getBankAccountByUserId(1L)).thenReturn(mockBankAccount);

        mockMvc.perform(get("/api/wallet/bank-account").header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.bankName").value("BCA"))
                .andExpect(jsonPath("$.accountNumber").value("8012341"))
                .andExpect(jsonPath("$.balance").value(5000));
    }

    @Test
    void testInitiateTopUp() throws Exception {
        id.ac.ui.cs.advprog.bidmartwalletservice.dto.TopUpInitiation mockResponse = id.ac.ui.cs.advprog.bidmartwalletservice.dto.TopUpInitiation.builder()
                .status("PENDING")
                .amountToPay(500L)
                .virtualAccount("00001")
                .build();
        when(walletService.initiateTopUp(1L, 500L)).thenReturn(mockResponse);

        mockMvc.perform(post("/api/wallet/topup/initiate")
                        .header("X-User-Id", 1L)
                        .param("amount", "500"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.amountToPay").value(500))
                .andExpect(jsonPath("$.virtualAccount").value("00001"));
    }

    @Test
    void testConfirmTopUp() throws Exception {
        Wallet updatedWallet = Wallet.builder().userId(1L).balance(1500L).version(1L).build();
        when(walletService.topUp(1L, 500L, "idempotency-key")).thenReturn(updatedWallet);

        mockMvc.perform(post("/api/wallet/topup/confirm")
                        .header("X-User-Id", 1L)
                        .param("amount", "500")
                        .header("X-Idempotency-Key", "idempotency-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500));
    }

    @Test
    void testSimulateBankPay() throws Exception {
        Wallet updatedWallet = Wallet.builder().userId(1L).balance(1500L).version(1L).build();
        when(walletService.topUp(1L, 500L, "payment-reference")).thenReturn(updatedWallet);

        mockMvc.perform(post("/api/wallet/topup/simulate-bank-pay")
                        .param("userId", "1")
                        .param("amount", "500")
                        .param("paymentReference", "payment-reference"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1500));
    }

    @Test
    void testWithdraw() throws Exception {
        Wallet updatedWallet = Wallet.builder().userId(1L).balance(800L).version(1L).build();

        when(walletService.withdraw(1L, 200L, "idempotency-key")).thenReturn(updatedWallet);

        mockMvc.perform(post("/api/wallet/withdraw")
                        .header("X-User-Id", 1L)
                        .param("amount", "200")
                        .header("X-Idempotency-Key", "idempotency-key"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(800));
    }

    @Test
    void testGetHistory() throws Exception {
        Transaction t1 = Transaction.builder()
                .id(1L).userId(1L).type(id.ac.ui.cs.advprog.bidmartwalletservice.model.TransactionType.TOPUP).amount(1000L)
                .description("Top-up").createdAt(LocalDateTime.now()).build();

        Transaction t2 = Transaction.builder()
                .id(2L).userId(1L).type(id.ac.ui.cs.advprog.bidmartwalletservice.model.TransactionType.WITHDRAW).amount(500L)
                .description("Transfer ke Bank").createdAt(LocalDateTime.now()).build();

        List<Transaction> mockHistory = Arrays.asList(t1, t2);

        when(walletService.getHistory(1L)).thenReturn(mockHistory);

        mockMvc.perform(get("/api/wallet/history").header("X-User-Id", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("TOPUP"))
                .andExpect(jsonPath("$[1].type").value("WITHDRAW"));
    }

    @Test
    void testHoldBalance() throws Exception {
        mockMvc.perform(post("/api/wallet/hold")
                        .param("userId", "1")
                        .param("amount", "300")
                        .header("X-Idempotency-Key", "idempotency-key"))
                .andExpect(status().isOk())
                .andExpect(content().string("Balance held successfully"));
    }

    @Test
    void testReleaseBalance() throws Exception {
        mockMvc.perform(post("/api/wallet/release")
                        .param("userId", "1")
                        .param("amount", "300")
                        .header("X-Idempotency-Key", "idempotency-key"))
                .andExpect(status().isOk())
                .andExpect(content().string("Balance released successfully"));
    }

    @Test
    void testSettlePayment() throws Exception {
        mockMvc.perform(post("/api/wallet/settle")
                        .param("buyerId", "1")
                        .param("sellerId", "2")
                        .param("amount", "300")
                        .header("X-Idempotency-Key", "idempotency-key"))
                .andExpect(status().isOk())
                .andExpect(content().string("Payment settled successfully"));
    }
}