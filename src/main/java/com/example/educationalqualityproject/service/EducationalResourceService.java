package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.entity.EducationalResource;
import com.example.educationalqualityproject.entity.Reservation;
import com.example.educationalqualityproject.entity.ReservationStatus;
import com.example.educationalqualityproject.entity.ResourceType;
import com.example.educationalqualityproject.repository.EducationalResourceRepository;
import com.example.educationalqualityproject.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class EducationalResourceService {

    private final EducationalResourceRepository repository;
    private final ReservationRepository reservationRepository;
    private final AuditRecorder auditRecorder;

    public EducationalResourceService(
            EducationalResourceRepository repository,
            ReservationRepository reservationRepository,
            AuditRecorder auditRecorder) {
        this.repository = repository;
        this.reservationRepository = reservationRepository;
        this.auditRecorder = auditRecorder;
    }

    public List<EducationalResource> search(
            ResourceType type,
            Integer minimumCapacity,
            String location,
            String competence,
            Instant availableFrom,
            Instant availableUntil) {
        if (minimumCapacity != null && minimumCapacity < 1) {
            throw new ResponseStatusException(BAD_REQUEST, "A capacidade mínima deve ser maior que zero.");
        }
        if ((availableFrom == null) != (availableUntil == null)) {
            throw new ResponseStatusException(BAD_REQUEST, "Informe início e fim da disponibilidade.");
        }
        if (availableFrom != null && !availableFrom.isBefore(availableUntil)) {
            throw new ResponseStatusException(BAD_REQUEST, "O início deve ser anterior ao fim.");
        }

        Set<String> unavailableIds = availableFrom == null ? Set.of()
                : reservationRepository.findByStatusInAndStartAtLessThanAndEndAtGreaterThan(
                                List.of(ReservationStatus.PENDING_APPROVAL, ReservationStatus.APPROVED),
                                availableUntil, availableFrom)
                        .stream()
                        .flatMap(reservation -> reservation.getResourceIds().stream())
                        .collect(Collectors.toSet());

        return repository.findAll().stream()
                .filter(resource -> type == null || resource.getType() == type)
                .filter(resource -> minimumCapacity == null
                        || resource.getCapacity() != null && resource.getCapacity() >= minimumCapacity)
                .filter(resource -> location == null || resource.getLocation() != null
                        && resource.getLocation().toLowerCase(Locale.ROOT)
                        .contains(location.toLowerCase(Locale.ROOT)))
                .filter(resource -> competence == null || resource.getCompetencies() != null
                        && resource.getCompetencies().stream()
                        .anyMatch(value -> value.toLowerCase(Locale.ROOT)
                                .contains(competence.toLowerCase(Locale.ROOT))))
                .filter(resource -> availableFrom == null || !resource.isUnderMaintenance())
                .filter(resource -> availableFrom == null || !unavailableIds.contains(resource.getId()))
                .toList();
    }

    public EducationalResource getById(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Recurso não encontrado."));
    }

    @Transactional
    public EducationalResource save(EducationalResource resource, String actor) {
        if (resource.getType() == ResourceType.SALA
                && (resource.getCapacity() == null || resource.getCapacity() < 1)) {
            throw new ResponseStatusException(BAD_REQUEST, "Sala deve ter capacidade maior que zero.");
        }
        EducationalResource previous = resource.getId() == null
                ? null : repository.findById(resource.getId()).orElse(null);
        EducationalResource saved = repository.save(resource);
        String action = previous == null ? "RESOURCE_CREATED" : "RESOURCE_UPDATED";
        String details = previous == null
                ? "Depois: " + describe(saved)
                : "Antes: " + describe(previous) + "; depois: " + describe(saved);
        auditRecorder.record("RESOURCE", saved.getId(), action, actor, details);
        return saved;
    }

    @Transactional
    public void delete(String id, String actor) {
        EducationalResource resource = getById(id);
        repository.deleteById(id);
        auditRecorder.record("RESOURCE", id, "RESOURCE_DELETED", actor,
                "Excluído: " + describe(resource));
    }

    private String describe(EducationalResource resource) {
        return "nome=" + resource.getName()
                + ", tipo=" + resource.getType()
                + ", local=" + resource.getLocation()
                + ", capacidade=" + resource.getCapacity()
                + ", descrição=" + resource.getDescription()
                + ", competências=" + resource.getCompetencies()
                + ", restrito=" + resource.isRestricted()
                + ", manutenção=" + resource.isUnderMaintenance();
    }
}
