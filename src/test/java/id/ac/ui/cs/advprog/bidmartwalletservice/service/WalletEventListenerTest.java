package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.config.RabbitMQConfig;
import id.ac.ui.cs.advprog.bidmartwalletservice.dto.WalletEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WalletEventListenerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private WalletEventListener walletEventListener;

    @Test
    void testHandleWalletEvent() {
        WalletEvent event = WalletEvent.builder()
                .userId(1L)
                .type("TOPUP")
                .amount(500L)
                .description("Desc")
                .timestamp("2026-05-18T10:00:00")
                .build();

        walletEventListener.handleWalletEvent(event);

        verify(rabbitTemplate).convertAndSend(
                RabbitMQConfig.WALLET_EXCHANGE,
                "wallet.event.topup",
                event
        );
    }
}
