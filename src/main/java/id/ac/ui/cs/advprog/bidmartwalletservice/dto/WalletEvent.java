package id.ac.ui.cs.advprog.bidmartwalletservice.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class WalletEvent {
    private Long userId;
    private String type;
    private Long amount;
    private String description;
    private String timestamp;
}