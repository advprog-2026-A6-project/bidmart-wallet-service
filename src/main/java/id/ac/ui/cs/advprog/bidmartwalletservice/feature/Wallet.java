package id.ac.ui.cs.advprog.bidmartwalletservice.feature;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ownerName;

    private Long balance;

    @PrePersist
    protected void onCreate() {
        if (this.balance == null) {
            this.balance = 0L;
        }
    }
}