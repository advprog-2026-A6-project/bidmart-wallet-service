package id.ac.ui.cs.advprog.bidmartwalletservice;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;

class BidmartWalletServiceApplicationTests {

    @Test
    void testMainMethod() {
        try (MockedStatic<SpringApplication> mockedSpringApplication = mockStatic(SpringApplication.class)) {
            mockedSpringApplication.when(() -> SpringApplication.run(eq(BidmartWalletServiceApplication.class), any(String[].class)))
                    .thenReturn(null);
            BidmartWalletServiceApplication.main(new String[] {});
            mockedSpringApplication.verify(() -> SpringApplication.run(eq(BidmartWalletServiceApplication.class), any(String[].class)));
        }
    }
}