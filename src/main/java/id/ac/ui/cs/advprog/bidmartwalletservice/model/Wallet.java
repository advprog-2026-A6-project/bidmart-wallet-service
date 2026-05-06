package id.ac.ui.cs.advprog.bidmartwalletservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "wallets")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class Wallet {
    @Id
    @Column(name = "user_id")
    private Long userId;

    private Long balance;

    @Column(name = "held_balance")
    private Long heldBalance;

    @Version
    private Long version;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.balance == null) this.balance = 0L;
        if (this.heldBalance == null) this.heldBalance = 0L;
    }
}