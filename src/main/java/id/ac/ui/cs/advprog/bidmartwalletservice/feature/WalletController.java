package id.ac.ui.cs.advprog.bidmartwalletservice.feature;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/dummy")
    public Wallet createDummy() {
        return walletService.createWallet("User Dummy " + System.currentTimeMillis(), 1000L);
    }

    @GetMapping
    public List<Wallet> getAll() {
        return walletService.getAllWallets();
    }
}