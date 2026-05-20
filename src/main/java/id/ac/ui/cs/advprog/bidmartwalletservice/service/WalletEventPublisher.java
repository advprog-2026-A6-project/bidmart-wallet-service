package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.dto.WalletEvent;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class WalletEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishEvent(Long userId, TransactionType type, Long amount, String description) {
        WalletEvent event = WalletEvent.builder()
                .userId(userId)
                .type(type.name())
                .amount(amount)
                .description(description)
                .timestamp(LocalDateTime.now().toString())
                .build();

        eventPublisher.publishEvent(event);
    }
}
