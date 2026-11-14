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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class MaterialCheckoutService {

    private static final String CHECKOUT = "RESOURCE_CHECKOUT";

    private final MaterialCheckoutRepository checkoutRepository;
    private final ReservationRepository reservationRepository;
    private final EducationalResourceRepository resourceRepository;
    private final AuditRecorder auditRecorder;
    private final NotificationSender notificationSender;

    public MaterialCheckoutService(
            MaterialCheckoutRepository checkoutRepository,
            ReservationRepository reservationRepository,
            EducationalResourceRepository resourceRepository,
            AuditRecorder auditRecorder,
            NotificationSender notificationSender) {
        this.checkoutRepository = checkoutRepository;
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.auditRecorder = auditRecorder;
        this.notificationSender = notificationSender;
    }

    @Transactional
    public MaterialCheckout withdraw(MaterialCheckoutRequest request, String actor) {
        Reservation reservation = reservationRepository.findById(request.reservationId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Reserva não encontrada."));
        if (reservation.getStatus() != ReservationStatus.APPROVED) {
            throw new ResponseStatusException(BAD_REQUEST, "O material só pode ser retirado de uma reserva aprovada.");
        }
        if (!reservation.getResourceIds().contains(request.resourceId())) {
            throw new ResponseStatusException(BAD_REQUEST, "O material não pertence a esta reserva.");
        }

        EducationalResource resource = resourceRepository.findById(request.resourceId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Recurso não encontrado."));
        if (resource.getType() != ResourceType.MATERIAL && resource.getType() != ResourceType.EQUIPAMENTO) {
            throw new ResponseStatusException(BAD_REQUEST,
                    "A retirada só pode ser registrada para materiais ou equipamentos.");
        }
        if (resource.isUnderMaintenance()) {
            throw new ResponseStatusException(BAD_REQUEST, "O recurso está em manutenção e não pode ser retirado.");
        }
        if (checkoutRepository.findByReservationIdAndResourceId(request.reservationId(), request.resourceId()).isPresent()) {
            throw new ResponseStatusException(CONFLICT, "A retirada deste material já foi registrada para a reserva.");
        }

        MaterialCheckout checkout = checkoutRepository.save(new MaterialCheckout(
                reservation.getId(), resource.getId(), reservation.getRequestedBy(), actor));
        auditRecorder.record(CHECKOUT, checkout.getId(), "RESOURCE_WITHDRAWN", actor,
                "Recurso " + resource.getName() + " retirado para a reserva " + reservation.getId() + ".");
        notificationSender.send(reservation.getRequestedBy(), "RESOURCE_WITHDRAWN", checkout.getId(),
                "A retirada de " + resource.getName() + " foi registrada.");
        return checkout;
    }

    @Transactional
    public MaterialCheckout returnMaterial(String checkoutId, String actor) {
        MaterialCheckout checkout = checkoutRepository.findById(checkoutId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Registro de retirada não encontrado."));
        if (checkout.getStatus() == MaterialCheckoutStatus.RETURNED) {
            throw new ResponseStatusException(CONFLICT, "A devolução deste material já foi registrada.");
        }

        checkout.setStatus(MaterialCheckoutStatus.RETURNED);
        checkout.setReturnedBy(actor);
        checkout.setReturnedAt(Instant.now());
        checkout = checkoutRepository.save(checkout);
        auditRecorder.record(CHECKOUT, checkout.getId(), "RESOURCE_RETURNED", actor,
                "Devolução do recurso " + checkout.getResourceId() + " registrada.");
        notificationSender.send(checkout.getRequester(), "RESOURCE_RETURNED", checkout.getId(),
                "A devolução do recurso foi registrada.");
        return checkout;
    }

    public List<MaterialCheckout> list() {
        return checkoutRepository.findAllByOrderByWithdrawnAtDesc();
    }
}
