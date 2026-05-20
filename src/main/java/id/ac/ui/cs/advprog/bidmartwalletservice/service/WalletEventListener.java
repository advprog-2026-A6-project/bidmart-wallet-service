package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.config.RabbitMQConfig;
import id.ac.ui.cs.advprog.bidmartwalletservice.dto.WalletEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class WalletEventListener {

    private final RabbitTemplate rabbitTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleWalletEvent(WalletEvent event) {
        String routingKey = "wallet.event." + event.getType().toLowerCase();
        rabbitTemplate.convertAndSend(RabbitMQConfig.WALLET_EXCHANGE, routingKey, event);
    }
}
