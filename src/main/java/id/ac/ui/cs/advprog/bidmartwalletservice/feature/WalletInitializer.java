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
        if (walletService.getAllWallets().isEmpty()) {
            walletService.createWallet("Initial Owner", 5000L);
            System.out.println(">>> Dummy Wallet Created!");
        }
    }
}
