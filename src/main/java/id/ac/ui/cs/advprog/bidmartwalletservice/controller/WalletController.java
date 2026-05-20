package id.ac.ui.cs.advprog.bidmartwalletservice.controller;

import id.ac.ui.cs.advprog.bidmartwalletservice.dto.TopUpInitiation;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Transaction;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import id.ac.ui.cs.advprog.bidmartwalletservice.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public ResponseEntity<Wallet> getWallet(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    @GetMapping("/bank-account")
    public ResponseEntity<id.ac.ui.cs.advprog.bidmartwalletservice.model.BankAccount> getBankAccount(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(walletService.getBankAccountByUserId(userId));
    }

    @PostMapping("/topup/initiate")
    public ResponseEntity<TopUpInitiation> initiateTopUp(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long amount) {
        return ResponseEntity.ok(walletService.initiateTopUp(userId, amount));
    }

    @PostMapping("/topup/confirm")
    public ResponseEntity<Wallet> confirmTopUp(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long amount,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {
        return ResponseEntity.ok(walletService.topUp(userId, amount, idempotencyKey));
    }

    @PostMapping("/topup/simulate-bank-pay")
    public ResponseEntity<Wallet> simulateBankPay(
            @RequestParam Long userId,
            @RequestParam Long amount,
            @RequestParam String paymentReference) {
        return ResponseEntity.ok(walletService.topUp(userId, amount, paymentReference));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Wallet> withdraw(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long amount,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {
        return ResponseEntity.ok(walletService.withdraw(userId, amount, idempotencyKey));
    }

    @GetMapping("/history")
    public ResponseEntity<List<Transaction>> getHistory(@RequestHeader("X-User-Id") Long userId) {
        return ResponseEntity.ok(walletService.getHistory(userId));
    }

    @PostMapping("/hold")
    public ResponseEntity<String> holdBalance(
            @RequestParam Long userId,
            @RequestParam Long amount,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {
        walletService.holdAmount(userId, amount, idempotencyKey);
        return ResponseEntity.ok("Balance held successfully");
    }

    @PostMapping("/release")
    public ResponseEntity<String> releaseBalance(
            @RequestParam Long userId,
            @RequestParam Long amount,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {
        walletService.releaseAmount(userId, amount, idempotencyKey);
        return ResponseEntity.ok("Balance released successfully");
    }

    @PostMapping("/settle")
    public ResponseEntity<String> settlePayment(
            @RequestParam Long buyerId,
            @RequestParam Long sellerId,
            @RequestParam Long amount,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {
        walletService.settlePayment(buyerId, sellerId, amount, idempotencyKey);
        return ResponseEntity.ok("Payment settled successfully");
    }
}