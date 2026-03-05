package id.ac.ui.cs.advprog.bidmartwalletservice.feature;

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

    @Version
    private Long version;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.balance == null) this.balance = 0L;
    }
}