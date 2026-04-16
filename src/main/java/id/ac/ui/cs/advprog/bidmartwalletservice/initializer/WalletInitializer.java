package id.ac.ui.cs.advprog.bidmartwalletservice.initializer;

import id.ac.ui.cs.advprog.bidmartwalletservice.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WalletInitializer implements CommandLineRunner {
    private final WalletService walletService;
    @Override
    public void run(String... args) {
        try {
            walletService.getWalletByUserId(1L);
            log.info(">>> Wallet for User 1 is ready!");
        } catch (Exception e) {
            log.error(">>> Error initializing wallet: {}", e.getMessage());
        }
    }
}