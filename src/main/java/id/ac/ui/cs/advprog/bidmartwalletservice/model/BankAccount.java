package id.ac.ui.cs.advprog.bidmartwalletservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "bank_accounts")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class BankAccount {
    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    private Long balance;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.balance == null) this.balance = 0L;
    }
}
