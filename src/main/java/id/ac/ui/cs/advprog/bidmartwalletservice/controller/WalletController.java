package id.ac.ui.cs.advprog.bidmartwalletservice.controller;

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
    @GetMapping("/{userId}")
    public ResponseEntity<Wallet> getWallet(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getWalletByUserId(userId));
    }

    @PostMapping("/{userId}/topup")
    public ResponseEntity<Wallet> topUp(
            @PathVariable Long userId,
            @RequestParam Long amount) {
        return ResponseEntity.ok(walletService.topUp(userId, amount));
    }
    @PostMapping("/{userId}/withdraw")
    public ResponseEntity<Wallet> withdraw(
            @PathVariable Long userId,
            @RequestParam Long amount,
            @RequestParam String bankAccount) {
        return ResponseEntity.ok(walletService.withdraw(userId, amount, bankAccount));
    }
    @GetMapping("/{userId}/history")
    public ResponseEntity<List<Transaction>> getHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(walletService.getHistory(userId));
    }

    @PostMapping("/{userId}/hold")
    public ResponseEntity<String> holdBalance(@PathVariable Long userId, @RequestParam Long amount) {
        walletService.holdAmount(userId, amount);
        return ResponseEntity.ok("Balance held successfully");
    }

    @PostMapping("/{userId}/release")
    public ResponseEntity<String> releaseBalance(@PathVariable Long userId, @RequestParam Long amount) {
        walletService.releaseAmount(userId, amount);
        return ResponseEntity.ok("Balance released successfully");
    }

    @PostMapping("/{userId}/settle")
    public ResponseEntity<String> settlePayment(@PathVariable Long userId, @RequestParam Long amount) {
        walletService.settlePayment(userId, amount);
        return ResponseEntity.ok("Payment settled successfully");
    }
}