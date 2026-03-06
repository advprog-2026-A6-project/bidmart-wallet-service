package id.ac.ui.cs.advprog.bidmartwalletservice.controller;

import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import id.ac.ui.cs.advprog.bidmartwalletservice.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

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
}