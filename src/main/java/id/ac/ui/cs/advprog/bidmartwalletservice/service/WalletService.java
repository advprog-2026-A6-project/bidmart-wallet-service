package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.dto.TopUpInitiation;
import id.ac.ui.cs.advprog.bidmartwalletservice.dto.WalletEvent;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.BankAccount;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Transaction;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletAccountService walletAccountService;
    private final WalletFundingService walletFundingService;
    private final WalletAuctionService walletAuctionService;
    private final WalletEventPublisher eventPublisher;

    public Wallet getWalletByUserId(Long userId) {
        return walletAccountService.getWalletByUserId(userId);
    }

    public BankAccount getBankAccountByUserId(Long userId) {
        return walletAccountService.getBankAccountByUserId(userId);
    }

    public TopUpInitiation initiateTopUp(Long userId, Long amount) {
        return walletFundingService.initiateTopUp(userId, amount);
    }

    public Wallet topUp(Long userId, Long amount, String idempotencyKey) {
        return walletFundingService.topUp(userId, amount, idempotencyKey);
    }

    public Wallet withdraw(Long userId, Long amount, String idempotencyKey) {
        return walletFundingService.withdraw(userId, amount, idempotencyKey);
    }

    public List<Transaction> getHistory(Long userId) {
        return walletAccountService.getHistory(userId);
    }

    public void holdAmount(Long userId, Long amount, String idempotencyKey) {
        walletAuctionService.holdAmount(userId, amount, idempotencyKey);
    }

    public void releaseAmount(Long userId, Long amount, String idempotencyKey) {
        walletAuctionService.releaseAmount(userId, amount, idempotencyKey);
    }

    public void settlePayment(Long buyerId, Long sellerId, Long amount, String idempotencyKey) {
        walletAuctionService.settlePayment(buyerId, sellerId, amount, idempotencyKey);
    }

    public void handleWalletEvent(WalletEvent event) {
        eventPublisher.publishEvent(event.getUserId(),
                id.ac.ui.cs.advprog.bidmartwalletservice.model.TransactionType.valueOf(event.getType()),
                event.getAmount(),
                event.getDescription());
    }
}