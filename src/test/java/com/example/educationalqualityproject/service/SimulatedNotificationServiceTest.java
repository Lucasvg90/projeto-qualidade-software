package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.entity.NotificationEvent;
import com.example.educationalqualityproject.repository.NotificationEventRepository;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SimulatedNotificationServiceTest {

    @Test
    void persistsNotificationWithSimulatedChannelWithoutExternalDelivery() {
        NotificationEventRepository repository = mock(NotificationEventRepository.class);
        when(repository.save(any(NotificationEvent.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        new SimulatedNotificationService(repository)
                .send("requester@example.com", "RESERVATION_CREATED", "reservation-1", "Reserva criada.");

        var captor = org.mockito.ArgumentCaptor.forClass(NotificationEvent.class);
        verify(repository).save(captor.capture());
        assertEquals("SIMULATED", captor.getValue().getChannel());
        assertEquals("requester@example.com", captor.getValue().getRecipient());
        assertEquals("RESERVATION_CREATED", captor.getValue().getEventType());
        assertEquals("Reserva criada.", captor.getValue().getMessage());
    }
}
