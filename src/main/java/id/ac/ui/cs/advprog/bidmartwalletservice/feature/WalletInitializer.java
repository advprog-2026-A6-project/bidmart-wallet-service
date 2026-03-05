package id.ac.ui.cs.advprog.bidmartwalletservice.feature;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WalletInitializer implements CommandLineRunner {
    private final WalletService walletService;

    @Override
    public void run(String... args) {
        try {
            walletService.getWalletByUserId(1L);
            System.out.println(">>> Wallet for User 1 is ready!");
        } catch (Exception e) {
            System.out.println(">>> Error initializing wallet: " + e.getMessage());
        }
    }
}