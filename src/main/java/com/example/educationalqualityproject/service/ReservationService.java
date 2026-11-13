package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.dto.ReservationRequest;
import com.example.educationalqualityproject.entity.EducationalResource;
import com.example.educationalqualityproject.entity.Reservation;
import com.example.educationalqualityproject.entity.ReservationSlot;
import com.example.educationalqualityproject.entity.ReservationStatus;
import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.repository.EducationalResourceRepository;
import com.example.educationalqualityproject.repository.ReservationRepository;
import com.example.educationalqualityproject.repository.ReservationSlotRepository;
import com.example.educationalqualityproject.repository.TeacherRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class ReservationService {

    private static final Duration MAX_DURATION = Duration.ofHours(24);
    private static final Duration SLOT_LENGTH = Duration.ofMinutes(1);
    private static final List<ReservationStatus> OCCUPYING_STATUSES =
            List.of(ReservationStatus.PENDING_APPROVAL, ReservationStatus.APPROVED);

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository slotRepository;
    private final EducationalResourceRepository resourceRepository;
    private final TeacherRepository teacherRepository;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationSlotRepository slotRepository,
            EducationalResourceRepository resourceRepository,
            TeacherRepository teacherRepository) {
        this.reservationRepository = reservationRepository;
        this.slotRepository = slotRepository;
        this.resourceRepository = resourceRepository;
        this.teacherRepository = teacherRepository;
    }

    @Transactional
    public Reservation create(ReservationRequest request, String requestedBy) {
        Reservation reservation = fromRequest(request, requestedBy, null);
        reservation.setStatus(hasRestrictedResource(reservation) 
                ? ReservationStatus.PENDING_APPROVAL : ReservationStatus.APPROVED);
        reservation = reservationRepository.save(reservation);
        acquireSlots(reservation);
        return reservation;
    }

    @Transactional
    public Reservation update(String id, ReservationRequest request, String actor, boolean administrator) {
        Reservation existing = get(id);
        verifyOwner(existing, actor, administrator);
        ensureEditable(existing);

        Reservation updated = fromRequest(request, existing.getRequestedBy(), existing);
        updated.setStatus(hasRestrictedResource(updated)
                ? ReservationStatus.PENDING_APPROVAL : ReservationStatus.APPROVED);
        slotRepository.deleteByReservationId(id);
        updated = reservationRepository.save(updated);
        acquireSlots(updated);
        return updated;
    }

    @Transactional
    public Reservation cancel(String id, String actor, boolean administrator) {
        Reservation reservation = get(id);
        verifyOwner(reservation, actor, administrator);
        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.REJECTED) {
            throw new ResponseStatusException(BAD_REQUEST, "Esta reserva já foi encerrada.");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        slotRepository.deleteByReservationId(id);
        return reservationRepository.save(reservation);
    }

    @Transactional
    public Reservation decide(String id, boolean approve, String note) {
        Reservation reservation = get(id);
        if (reservation.getStatus() != ReservationStatus.PENDING_APPROVAL) {
            throw new ResponseStatusException(BAD_REQUEST, "A reserva não aguarda aprovação.");
        }
        reservation.setStatus(approve ? ReservationStatus.APPROVED : ReservationStatus.REJECTED);
        reservation.setDecisionNote(note);
        if (!approve) {
            slotRepository.deleteByReservationId(id);
        }
        return reservationRepository.save(reservation);
    }

    public List<Reservation> getAll() {
        return reservationRepository.findAll();
    }

    public List<Reservation> getForRequester(String requester) {
        return reservationRepository.findAll().stream()
                .filter(reservation -> requester.equals(reservation.getRequestedBy()))
                .toList();
    }

    public Reservation getForRequester(String id, String requester) {
        Reservation reservation = get(id);
        verifyOwner(reservation, requester, false);
        ensureEditable(reservation);
        return reservation;
    }

    public Reservation get(String id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Reserva não encontrada."));
    }

    private Reservation fromRequest(ReservationRequest request, String requester, Reservation existing) {
        validateInterval(request.startAt(), request.endAt());
        List<String> resourceIds = request.resourceIds().stream().distinct().toList();
        if (resourceIds.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Selecione ao menos um recurso para reservar.");
        }
        if (resourceIds.size() != request.resourceIds().size()) {
            throw new ResponseStatusException(BAD_REQUEST, "A lista contém recursos duplicados.");
        }
        if (resourceIds.stream().anyMatch(id -> !resourceRepository.existsById(id))) {
            throw new ResponseStatusException(NOT_FOUND, "Um ou mais recursos não foram encontrados.");
        }
        if (request.teacherId() != null && !request.teacherId().isBlank()
                && !teacherRepository.existsById(request.teacherId())) {
            throw new ResponseStatusException(NOT_FOUND, "Professor não encontrado.");
        }

        Reservation reservation = existing == null ? new Reservation() : existing;
        reservation.setResourceIds(resourceIds);
        reservation.setTeacherId(request.teacherId() == null || request.teacherId().isBlank()
                ? null : request.teacherId());
        reservation.setRequestedBy(requester);
        reservation.setStartAt(request.startAt());
        reservation.setEndAt(request.endAt());
        return reservation;
    }

    private void validateInterval(Instant start, Instant end) {
        if (!start.isBefore(end) || Duration.between(start, end).compareTo(MAX_DURATION) > 0) {
            throw new ResponseStatusException(BAD_REQUEST, "A duração deve ser positiva e de no máximo 24 horas.");
        }
        if (start.getNano() != 0 || end.getNano() != 0
                || start.getEpochSecond() % 60 != 0 || end.getEpochSecond() % 60 != 0) {
            throw new ResponseStatusException(BAD_REQUEST, "Os horários devem estar alinhados a minutos inteiros.");
        }
        if (start.isBefore(Instant.now())) {
            throw new ResponseStatusException(BAD_REQUEST, "Não é possível reservar no passado.");
        }
    }

    private boolean hasRestrictedResource(Reservation reservation) {
        return resourceRepository.findAllById(reservation.getResourceIds()).stream()
                .anyMatch(EducationalResource::isRestricted);
    }

    private void acquireSlots(Reservation reservation) {
        Set<String> resourceKeys = new HashSet<>();
        reservation.getResourceIds().forEach(id -> resourceKeys.add("resource:" + id));
        if (reservation.getTeacherId() != null) {
            resourceKeys.add("teacher:" + reservation.getTeacherId());
        }

        List<ReservationSlot> slots = new ArrayList<>();
        for (String resourceKey : resourceKeys) {
            for (Instant slotStart = reservation.getStartAt();
                 slotStart.isBefore(reservation.getEndAt());
                 slotStart = slotStart.plus(SLOT_LENGTH)) {
                slots.add(new ReservationSlot(resourceKey, slotStart, reservation.getId()));
            }
        }
        slotRepository.saveAll(slots);
    }

    private void verifyOwner(Reservation reservation, String actor, boolean administrator) {
        if (!administrator && !actor.equals(reservation.getRequestedBy())) {
            throw new AccessDeniedException("Você não pode alterar esta reserva.");
        }
    }

    private void ensureEditable(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.REJECTED) {
            throw new ResponseStatusException(BAD_REQUEST, "Esta reserva não pode mais ser alterada.");
        }
    }
}
