package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.model.Transaction;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.TransactionRepository;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findById(userId)
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder().userId(userId).balance(0L).build()
                ));
    }

    @Transactional
    public Wallet topUp(Long userId, Long amount) {
        if (amount <= 0) throw new IllegalArgumentException("Top-up must be > 0");
        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance() + amount);

        transactionRepository.save(Transaction.builder()
                .userId(userId).type("TOPUP").amount(amount)
                .description("Top-up via System").build());

        return walletRepository.save(wallet);
    }

    @Transactional
    public Wallet withdraw(Long userId, Long amount, String bankAccount) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal must be > 0");

        Wallet wallet = getWalletByUserId(userId);

        if (wallet.getBalance() < amount) {
            throw new IllegalStateException("Saldo tidak mencukupi untuk penarikan");
        }

        if (bankAccount == null || bankAccount.isEmpty()) {
            throw new IllegalArgumentException("Nomor rekening bank harus diisi");
        }

        wallet.setBalance(wallet.getBalance() - amount);

        transactionRepository.save(Transaction.builder()
                .userId(userId).type("WITHDRAW").amount(amount)
                .description("Transfer ke Bank: " + bankAccount).build());

        return walletRepository.save(wallet);
    }

    public List<Transaction> getHistory(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional
    public void holdAmount(Long userId, Long amount) {
        Wallet wallet = getWalletByUserId(userId);

        if (wallet.getBalance() < amount) {
            throw new IllegalStateException("Saldo tidak cukup untuk melakukan bid");
        }

        wallet.setBalance(wallet.getBalance() - amount);
        wallet.setHeldBalance(wallet.getHeldBalance() + amount);

        transactionRepository.save(Transaction.builder()
                .userId(userId).type("HOLD").amount(amount)
                .description("Dana ditahan untuk penawaran lelang").build());

        walletRepository.save(wallet);
    }

    @Transactional
    public void releaseAmount(Long userId, Long amount) {
        Wallet wallet = getWalletByUserId(userId);

        wallet.setHeldBalance(wallet.getHeldBalance() - amount);
        wallet.setBalance(wallet.getBalance() + amount);

        transactionRepository.save(Transaction.builder()
                .userId(userId).type("RELEASE").amount(amount)
                .description("Dana dilepaskan karena bid kalah").build());

        walletRepository.save(wallet);
    }

    @Transactional
    public void settlePayment(Long userId, Long amount) {
        Wallet wallet = getWalletByUserId(userId);

        if (wallet.getHeldBalance() < amount) {
            throw new IllegalStateException("Data held balance tidak konsisten!");
        }
        wallet.setHeldBalance(wallet.getHeldBalance() - amount);

        transactionRepository.save(Transaction.builder()
                .userId(userId).type("PAYMENT").amount(amount)
                .description("Pembayaran lelang dimenangkan").build());

        walletRepository.save(wallet);
    }
}