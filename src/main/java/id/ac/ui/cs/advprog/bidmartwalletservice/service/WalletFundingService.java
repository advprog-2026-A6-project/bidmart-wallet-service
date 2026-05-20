package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.dto.TopUpInitiation;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.BankAccount;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Transaction;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.TransactionType;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.BankAccountRepository;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.TransactionRepository;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletFundingService {

    private final WalletAccountService walletAccountService;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final BankAccountRepository bankAccountRepository;
    private final WalletEventPublisher eventPublisher;

    /**
     * Generates a virtual account initiation response for a top-up request.
     * Business logic for VA number generation belongs in the service layer, not the controller.
     */
    public TopUpInitiation initiateTopUp(Long userId, Long amount) {
        String virtualAccountNumber = "0000" + userId;
        String paymentReference = UUID.randomUUID().toString();

        return TopUpInitiation.builder()
                .status("PENDING")
                .virtualAccountNumber(virtualAccountNumber)
                .virtualAccount(virtualAccountNumber)
                .paymentReference(paymentReference)
                .amountToPay(amount)
                .build();
    }

    @Transactional
    public Wallet topUp(Long userId, Long amount, String idempotencyKey) {
        if (amount <= 0) throw new IllegalArgumentException("Top-up must be > 0");
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Idempotency key wajib disertakan");
        }

        Optional<Transaction> existingTx = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTx.isPresent()) {
            return walletAccountService.getWalletByUserId(userId);
        }

        BankAccount bankAccount = walletAccountService.getBankAccountByUserId(userId);

        if (bankAccount.getBalance() < amount) {
            throw new IllegalArgumentException("Saldo rekening bank simulasi tidak mencukupi!");
        }

        bankAccount.setBalance(bankAccount.getBalance() - amount);
        bankAccountRepository.save(bankAccount);

        Wallet wallet = walletAccountService.getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance() + amount);
        Wallet savedWallet = walletRepository.save(wallet);

        String txDescription = "Top-up via Bank " + bankAccount.getBankName();
        transactionRepository.save(Transaction.builder()
                .userId(userId)
                .type(TransactionType.TOPUP)
                .amount(amount)
                .balanceAfter(wallet.getBalance())
                .heldBalanceAfter(wallet.getHeldBalance())
                .idempotencyKey(idempotencyKey)
                .description(txDescription)
                .build());

        eventPublisher.publishEvent(userId, TransactionType.TOPUP, amount, txDescription);

        return savedWallet;
    }

    @Transactional
    public Wallet withdraw(Long userId, Long amount, String idempotencyKey) {
        if (amount <= 0) throw new IllegalArgumentException("Withdraw must be > 0");
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Idempotency key wajib disertakan");
        }

        Optional<Transaction> existingTx = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTx.isPresent()) {
            return walletAccountService.getWalletByUserId(userId);
        }

        Wallet wallet = walletAccountService.getWalletByUserId(userId);
        if (wallet.getBalance() < amount) {
            throw new IllegalArgumentException("Saldo wallet tidak mencukupi untuk melakukan withdraw!");
        }

        BankAccount bankAccount = walletAccountService.getBankAccountByUserId(userId);

        wallet.setBalance(wallet.getBalance() - amount);
        bankAccount.setBalance(bankAccount.getBalance() + amount);

        walletRepository.save(wallet);
        bankAccountRepository.save(bankAccount);

        String description = "Withdraw ke Bank " + bankAccount.getBankName();
        transactionRepository.save(Transaction.builder()
                .userId(userId)
                .type(TransactionType.WITHDRAW)
                .amount(amount)
                .balanceAfter(wallet.getBalance())
                .heldBalanceAfter(wallet.getHeldBalance())
                .idempotencyKey(idempotencyKey)
                .description(description)
                .build());

        eventPublisher.publishEvent(userId, TransactionType.WITHDRAW, amount, description);

        return wallet;
    }
}
