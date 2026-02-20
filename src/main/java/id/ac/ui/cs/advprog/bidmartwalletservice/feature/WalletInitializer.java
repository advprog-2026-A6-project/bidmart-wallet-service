package id.ac.ui.cs.advprog.bidmartwalletservice.feature;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletInitializer implements CommandLineRunner {

    private final WalletService walletService;

    @Override
    public void run(String... args) {
        if (walletService.getAllWallets().isEmpty()) {
            walletService.createWallet("Initial Owner", 5000L);

            // GANTI System.out.println dengan log.info
            log.info(">>> Dummy Wallet Created!");
        }
    }
}