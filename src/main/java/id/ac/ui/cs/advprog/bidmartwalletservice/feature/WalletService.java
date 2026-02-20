package id.ac.ui.cs.advprog.bidmartwalletservice.feature;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

    public Wallet createWallet(String name, Long initialBalance) {
        Wallet wallet = Wallet.builder()
                .ownerName(name)
                .balance(initialBalance)
                .build();
        return walletRepository.save(wallet);
    }

    public List<Wallet> getAllWallets() {
        return walletRepository.findAll();
    }
}