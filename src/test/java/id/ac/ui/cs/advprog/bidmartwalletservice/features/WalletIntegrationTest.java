package id.ac.ui.cs.advprog.bidmartwalletservice.features;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.*;
import org.springframework.test.context.TestPropertySource;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class WalletIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAllWallets() throws Exception {
        mockMvc.perform(get("/api/wallets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", isA(java.util.List.class)));
    }

    @Test
    void testCreateDummyWallet() throws Exception {
        mockMvc.perform(post("/api/wallets/dummy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ownerName", containsString("User Dummy")))
                .andExpect(jsonPath("$.balance", is(1000)));
    }

    @Test
    void testCreateDummyWalletAppearsInGetAll() throws Exception {
        mockMvc.perform(post("/api/wallets/dummy"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/wallets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThan(0))));
    }
}
