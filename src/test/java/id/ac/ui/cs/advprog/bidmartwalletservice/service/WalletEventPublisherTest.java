package id.ac.ui.cs.advprog.bidmartwalletservice.service;

import id.ac.ui.cs.advprog.bidmartwalletservice.dto.WalletEvent;
import id.ac.ui.cs.advprog.bidmartwalletservice.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WalletEventPublisherTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WalletEventPublisher walletEventPublisher;

    @Test
    void testPublishEvent() {
        walletEventPublisher.publishEvent(1L, TransactionType.TOPUP, 500L, "Desc");

        ArgumentCaptor<WalletEvent> eventCaptor = ArgumentCaptor.forClass(WalletEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());

        WalletEvent event = eventCaptor.getValue();
        assertEquals(1L, event.getUserId());
        assertEquals("TOPUP", event.getType());
        assertEquals(500L, event.getAmount());
        assertEquals("Desc", event.getDescription());
        assertNotNull(event.getTimestamp());
    }
}
