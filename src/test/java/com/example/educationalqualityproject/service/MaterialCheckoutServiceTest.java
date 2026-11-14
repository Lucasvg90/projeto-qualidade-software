package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.dto.MaterialCheckoutRequest;
import com.example.educationalqualityproject.entity.EducationalResource;
import com.example.educationalqualityproject.entity.MaterialCheckout;
import com.example.educationalqualityproject.entity.MaterialCheckoutStatus;
import com.example.educationalqualityproject.entity.Reservation;
import com.example.educationalqualityproject.entity.ReservationStatus;
import com.example.educationalqualityproject.entity.ResourceType;
import com.example.educationalqualityproject.repository.EducationalResourceRepository;
import com.example.educationalqualityproject.repository.MaterialCheckoutRepository;
import com.example.educationalqualityproject.repository.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaterialCheckoutServiceTest {

    @Mock
    private MaterialCheckoutRepository checkoutRepository;
    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private EducationalResourceRepository resourceRepository;
    @Mock
    private AuditRecorder auditRecorder;
    @Mock
    private NotificationSender notificationSender;
    @InjectMocks
    private MaterialCheckoutService checkoutService;

    @Test
    void recordsWithdrawalForEquipmentInApprovedReservation() {
        Reservation reservation = new Reservation();
        reservation.setId("reservation-1");
        reservation.setRequestedBy("requester@example.com");
        reservation.setResourceIds(List.of("equipment-1"));
        reservation.setStatus(ReservationStatus.APPROVED);
        EducationalResource equipment = new EducationalResource();
        equipment.setId("equipment-1");
        equipment.setName("Projetor");
        equipment.setType(ResourceType.EQUIPAMENTO);

        when(reservationRepository.findById("reservation-1")).thenReturn(Optional.of(reservation));
        when(resourceRepository.findById("equipment-1")).thenReturn(Optional.of(equipment));
        when(checkoutRepository.findByReservationIdAndResourceId("reservation-1", "equipment-1"))
                .thenReturn(Optional.empty());
        when(checkoutRepository.save(any(MaterialCheckout.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        MaterialCheckout result = checkoutService.withdraw(
                new MaterialCheckoutRequest("reservation-1", "equipment-1"), "admin@example.com");

        assertEquals(MaterialCheckoutStatus.WITHDRAWN, result.getStatus());
        assertEquals("admin@example.com", result.getWithdrawnBy());
        verify(auditRecorder).record("RESOURCE_CHECKOUT", result.getId(), "RESOURCE_WITHDRAWN",
                "admin@example.com", "Recurso Projetor retirado para a reserva reservation-1.");
        verify(notificationSender).send("requester@example.com", "RESOURCE_WITHDRAWN", result.getId(),
                "A retirada de Projetor foi registrada.");
    }

    @Test
    void recordsReturnActorAndTime() {
        MaterialCheckout checkout = new MaterialCheckout(
                "reservation-1", "material-1", "requester@example.com", "admin@example.com");
        checkout.setId("checkout-1");
        when(checkoutRepository.findById("checkout-1")).thenReturn(Optional.of(checkout));
        when(checkoutRepository.save(checkout)).thenReturn(checkout);

        MaterialCheckout result = checkoutService.returnMaterial("checkout-1", "admin@example.com");

        assertEquals(MaterialCheckoutStatus.RETURNED, result.getStatus());
        assertEquals("admin@example.com", result.getReturnedBy());
        assertEquals(Instant.now().getEpochSecond(), result.getReturnedAt().getEpochSecond());
        verify(auditRecorder).record("RESOURCE_CHECKOUT", "checkout-1", "RESOURCE_RETURNED",
                "admin@example.com", "Devolução do recurso material-1 registrada.");
    }

    @Test
    void preventsWithdrawalOfEquipmentUnderMaintenance() {
        Reservation reservation = new Reservation();
        reservation.setId("reservation-1");
        reservation.setRequestedBy("requester@example.com");
        reservation.setResourceIds(List.of("equipment-1"));
        reservation.setStatus(ReservationStatus.APPROVED);
        EducationalResource equipment = new EducationalResource();
        equipment.setId("equipment-1");
        equipment.setType(ResourceType.EQUIPAMENTO);
        equipment.setUnderMaintenance(true);

        when(reservationRepository.findById("reservation-1")).thenReturn(Optional.of(reservation));
        when(resourceRepository.findById("equipment-1")).thenReturn(Optional.of(equipment));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> checkoutService.withdraw(
                        new MaterialCheckoutRequest("reservation-1", "equipment-1"), "admin@example.com"));

        assertEquals("O recurso está em manutenção e não pode ser retirado.", exception.getReason());
        verifyNoInteractions(checkoutRepository);
    }
}
