package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.model.BankAccount;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Transaction;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.BankAccountRepository;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.TransactionRepository;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletAccountService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;

    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findById(userId)
                .orElseGet(() -> createWallet(userId));
    }

    public Wallet getWalletByUserIdForUpdate(Long userId) {
        return walletRepository.findByIdForUpdate(userId)
                .orElseGet(() -> createWallet(userId));
    }

    public BankAccount getBankAccountByUserId(Long userId) {
        return bankAccountRepository.findById(userId)
                .orElseGet(() -> {
                    BankAccount newBank = BankAccount.builder()
                            .userId(userId)
                            .bankName("BCA") 
                            .accountNumber("801234" + userId) 
                            .balance(0L) 
                            .build();
                    return bankAccountRepository.save(newBank);
                });
    }

    public List<Transaction> getHistory(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    private Wallet createWallet(Long userId) {
        return walletRepository.save(
                Wallet.builder().userId(userId).balance(0L).heldBalance(0L).build()
        );
    }
}
