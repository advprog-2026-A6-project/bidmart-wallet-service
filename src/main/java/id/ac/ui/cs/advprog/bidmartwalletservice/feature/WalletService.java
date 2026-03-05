package id.ac.ui.cs.advprog.bidmartwalletservice.feature;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findById(userId)
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder()
                                .userId(userId)
                                .balance(0L)
                                .build()
                ));
    }

    @Transactional
    public Wallet topUp(Long userId, Long amount) {
        if (amount <= 0) throw new IllegalArgumentException("Top-up must be > 0");
        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance() + amount);
        return walletRepository.save(wallet);
    }
}