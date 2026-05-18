package id.ac.ui.cs.advprog.bidmartwalletservice.controller;

import id.ac.ui.cs.advprog.bidmartwalletservice.model.Transaction;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.Wallet;
import id.ac.ui.cs.advprog.bidmartwalletservice.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

    @PostMapping("/topup/initiate")
    public ResponseEntity<Map<String, Object>> initiateTopUp(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long amount) {
        

        String virtualAccountNumber = "0000" + userId;
        String paymentReference = java.util.UUID.randomUUID().toString();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "PENDING");
        response.put("virtualAccount", virtualAccountNumber);
        response.put("amountToPay", amount);
        response.put("paymentReference", paymentReference);
        response.put("message", "Silakan bayar melalui nomor Virtual Account tersebut");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/topup/simulate-bank-pay")
    public ResponseEntity<Wallet> simulateBankPayment(
            @RequestParam Long userId,
            @RequestParam Long amount,
            @RequestParam String paymentReference) {
        
        Wallet updatedWallet = walletService.topUp(userId, amount, paymentReference);
        return ResponseEntity.ok(updatedWallet);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<Wallet> withdraw(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam Long amount,
            @RequestParam String bankAccount,
            @RequestHeader("X-Idempotency-Key") String idempotencyKey) {
        return ResponseEntity.ok(walletService.withdraw(userId, amount, bankAccount, idempotencyKey));
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