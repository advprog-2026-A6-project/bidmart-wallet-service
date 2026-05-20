package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.model.Transaction;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.TransactionType;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.TransactionRepository;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletAuctionService {

    private final WalletAccountService walletAccountService;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final WalletEventPublisher eventPublisher;

    @Transactional
    public void holdAmount(Long userId, Long amount, String idempotencyKey) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0");
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Idempotency key wajib disertakan");
        }

        Optional<Transaction> existingTx = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTx.isPresent()) {
            return;
        }

        Wallet wallet = walletAccountService.getWalletByUserId(userId);
        if (wallet.getBalance() < amount) {
            throw new IllegalStateException("Saldo tidak cukup untuk melakukan bid");
        }

        wallet.setBalance(wallet.getBalance() - amount);
        wallet.setHeldBalance(wallet.getHeldBalance() + amount);

        transactionRepository.save(Transaction.builder()
                .userId(userId)
                .type(TransactionType.HOLD)
                .amount(amount)
                .balanceAfter(wallet.getBalance())
                .heldBalanceAfter(wallet.getHeldBalance())
                .idempotencyKey(idempotencyKey)
                .description("Dana ditahan untuk penawaran lelang")
                .build());

        walletRepository.save(wallet);
        eventPublisher.publishEvent(userId, TransactionType.HOLD, amount, "Dana ditahan untuk penawaran lelang");
    }

    @Transactional
    public void releaseAmount(Long userId, Long amount, String idempotencyKey) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0");
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Idempotency key wajib disertakan");
        }

        Optional<Transaction> existingTx = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTx.isPresent()) {
            return;
        }

        Wallet wallet = walletAccountService.getWalletByUserId(userId);
        if (wallet.getHeldBalance() < amount) {
            throw new IllegalStateException("Data held balance tidak konsisten untuk release!");
        }

        wallet.setHeldBalance(wallet.getHeldBalance() - amount);
        wallet.setBalance(wallet.getBalance() + amount);

        transactionRepository.save(Transaction.builder()
                .userId(userId)
                .type(TransactionType.RELEASE)
                .amount(amount)
                .balanceAfter(wallet.getBalance())
                .heldBalanceAfter(wallet.getHeldBalance())
                .idempotencyKey(idempotencyKey)
                .description("Dana dilepaskan karena bid kalah")
                .build());

        walletRepository.save(wallet);
        eventPublisher.publishEvent(userId, TransactionType.RELEASE, amount, "Dana dilepaskan karena bid kalah");
    }

    @Transactional
    public void settlePayment(Long buyerId, Long sellerId, Long amount, String idempotencyKey) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be > 0");
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Idempotency key wajib disertakan");
        }

        Optional<Transaction> existingTx = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTx.isPresent()) {
            return;
        }

        Wallet buyerWallet = walletAccountService.getWalletByUserId(buyerId);
        if (buyerWallet.getHeldBalance() < amount) {
            throw new IllegalStateException("Data held balance pembeli tidak konsisten!");
        }
        buyerWallet.setHeldBalance(buyerWallet.getHeldBalance() - amount);

        Wallet sellerWallet = walletAccountService.getWalletByUserId(sellerId);
        sellerWallet.setBalance(sellerWallet.getBalance() + amount);

        walletRepository.save(buyerWallet);
        walletRepository.save(sellerWallet);

        transactionRepository.save(Transaction.builder()
                .userId(buyerId)
                .type(TransactionType.PAYMENT)
                .amount(amount)
                .balanceAfter(buyerWallet.getBalance())
                .heldBalanceAfter(buyerWallet.getHeldBalance())
                .idempotencyKey(idempotencyKey)
                .description("Pembayaran lelang dimenangkan")
                .build());

        transactionRepository.save(Transaction.builder()
                .userId(sellerId)
                .type(TransactionType.RECEIPT)
                .amount(amount)
                .balanceAfter(sellerWallet.getBalance())
                .heldBalanceAfter(sellerWallet.getHeldBalance())
                .idempotencyKey(idempotencyKey + "_SELLER")
                .description("Penerimaan dana dari hasil lelang")
                .build());

        eventPublisher.publishEvent(buyerId, TransactionType.PAYMENT, amount, "Pembayaran lelang dimenangkan");
        eventPublisher.publishEvent(sellerId, TransactionType.RECEIPT, amount, "Penerimaan dana dari hasil lelang");
    }
}
