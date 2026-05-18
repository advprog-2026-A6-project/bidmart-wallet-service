package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.config.RabbitMQConfig;
import id.ac.ui.cs.advprog.bidmartwalletservice.dto.WalletEvent;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Transaction;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.TransactionRepository;
import id.ac.ui.cs.advprog.bidmartwalletservice.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ApplicationEventPublisher eventPublisher;

    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findById(userId)
                .orElseGet(() -> walletRepository.save(
                        Wallet.builder().userId(userId).balance(0L).heldBalance(0L).build()
                ));
    }

    @Transactional
    public Wallet topUp(Long userId, Long amount, String idempotencyKey) {
        if (amount <= 0) throw new IllegalArgumentException("Top-up must be > 0");
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Idempotency key wajib disertakan");
        }

        Optional<Transaction> existingTx = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTx.isPresent()) {
            return getWalletByUserId(userId);
        }

        Wallet wallet = getWalletByUserId(userId);
        wallet.setBalance(wallet.getBalance() + amount);

        transactionRepository.save(Transaction.builder()
                .userId(userId).type("TOPUP").amount(amount)
                .balanceAfter(wallet.getBalance())
                .heldBalanceAfter(wallet.getHeldBalance())
                .idempotencyKey(idempotencyKey)
                .description("Top-up via System").build());

        Wallet savedWallet = walletRepository.save(wallet);
        publishEvent(userId, "TOPUP", amount, "Top-up via System");
        return savedWallet;
    }

    @Transactional
    public Wallet withdraw(Long userId, Long amount, String bankAccount, String idempotencyKey) {
        if (amount <= 0) throw new IllegalArgumentException("Withdrawal must be > 0");
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Idempotency key wajib disertakan");
        }

        Optional<Transaction> existingTx = transactionRepository.findByIdempotencyKey(idempotencyKey);
        if (existingTx.isPresent()) {
            return getWalletByUserId(userId);
        }

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
                .balanceAfter(wallet.getBalance())
                .heldBalanceAfter(wallet.getHeldBalance())
                .idempotencyKey(idempotencyKey)
                .description("Transfer ke Bank: " + bankAccount).build());

        Wallet savedWallet = walletRepository.save(wallet);
        publishEvent(userId, "WITHDRAW", amount, "Transfer ke Bank: " + bankAccount);
        return savedWallet;
    }

    public List<Transaction> getHistory(Long userId) {
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

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

        Wallet wallet = getWalletByUserId(userId);
        if (wallet.getBalance() < amount) {
            throw new IllegalStateException("Saldo tidak cukup untuk melakukan bid");
        }

        wallet.setBalance(wallet.getBalance() - amount);
        wallet.setHeldBalance(wallet.getHeldBalance() + amount);

        transactionRepository.save(Transaction.builder()
                .userId(userId).type("HOLD").amount(amount)
                .balanceAfter(wallet.getBalance())
                .heldBalanceAfter(wallet.getHeldBalance())
                .idempotencyKey(idempotencyKey) 
                .description("Dana ditahan untuk penawaran lelang").build());

        walletRepository.save(wallet);
        publishEvent(userId, "HOLD", amount, "Dana ditahan untuk penawaran lelang");
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

        Wallet wallet = getWalletByUserId(userId);
        if (wallet.getHeldBalance() < amount) {
            throw new IllegalStateException("Data held balance tidak konsisten untuk release!");
        }

        wallet.setHeldBalance(wallet.getHeldBalance() - amount);
        wallet.setBalance(wallet.getBalance() + amount);

        transactionRepository.save(Transaction.builder()
                .userId(userId).type("RELEASE").amount(amount)
                .balanceAfter(wallet.getBalance())
                .heldBalanceAfter(wallet.getHeldBalance())
                .idempotencyKey(idempotencyKey)
                .description("Dana dilepaskan karena bid kalah").build());

        walletRepository.save(wallet);
        publishEvent(userId, "RELEASE", amount, "Dana dilepaskan karena bid kalah");
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

        Wallet buyerWallet = getWalletByUserId(buyerId);
        if (buyerWallet.getHeldBalance() < amount) {
            throw new IllegalStateException("Data held balance pembeli tidak konsisten!");
        }
        buyerWallet.setHeldBalance(buyerWallet.getHeldBalance() - amount);

        Wallet sellerWallet = getWalletByUserId(sellerId);
        sellerWallet.setBalance(sellerWallet.getBalance() + amount);

        walletRepository.save(buyerWallet);
        walletRepository.save(sellerWallet);

        transactionRepository.save(Transaction.builder()
                .userId(buyerId)
                .type("PAYMENT")
                .amount(amount)
                .balanceAfter(buyerWallet.getBalance())
                .heldBalanceAfter(buyerWallet.getHeldBalance())
                .idempotencyKey(idempotencyKey) 
                .description("Pembayaran lelang dimenangkan").build());
        transactionRepository.save(Transaction.builder()
                .userId(sellerId)
                .type("RECEIPT")
                .amount(amount)
                .balanceAfter(sellerWallet.getBalance())
                .heldBalanceAfter(sellerWallet.getHeldBalance())
                .idempotencyKey(idempotencyKey + "_SELLER") 
                .description("Penerimaan dana dari hasil lelang").build());
        publishEvent(buyerId, "PAYMENT", amount, "Pembayaran lelang dimenangkan");
        publishEvent(sellerId, "RECEIPT", amount, "Penerimaan dana dari hasil lelang");
    }

    private void publishEvent(Long userId, String type, Long amount, String description) {
        WalletEvent event = WalletEvent.builder()
                .userId(userId)
                .type(type)
                .amount(amount)
                .description(description)
                .timestamp(LocalDateTime.now().toString())
                .build();

        eventPublisher.publishEvent(event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWalletEvent(WalletEvent event) {
        String routingKey = "wallet.event." + event.getType().toLowerCase();
        rabbitTemplate.convertAndSend(RabbitMQConfig.WALLET_EXCHANGE, routingKey, event);
    }
}