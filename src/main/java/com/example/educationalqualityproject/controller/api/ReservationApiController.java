package com.example.educationalqualityproject.controller.api;

import com.mongodb.MongoException;
import com.example.educationalqualityproject.dto.ReservationRequest;
import com.example.educationalqualityproject.entity.Reservation;
import com.example.educationalqualityproject.service.AuditRecorder;
import com.example.educationalqualityproject.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.UncategorizedMongoDbException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationApiController {

    private final ReservationService reservationService;
    private final AuditRecorder auditRecorder;

    public ReservationApiController(ReservationService reservationService, AuditRecorder auditRecorder) {
        this.reservationService = reservationService;
        this.auditRecorder = auditRecorder;
    }

    @GetMapping
    public List<Reservation> list(Authentication authentication) {
        return isAdministrator(authentication)
                ? reservationService.getAll()
                : reservationService.getForRequester(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Reservation create(@Valid @RequestBody ReservationRequest request, Authentication authentication) {
        try {
            return reservationService.create(request, authentication.getName());
        } catch (DuplicateKeyException exception) {
            auditRecorder.recordPreventedConflicts(request.resourceIds(), authentication.getName());
            throw exception;
        } catch (UncategorizedMongoDbException exception) {
            if (!isTransientTransactionConflict(exception)) {
                throw exception;
            }
            auditRecorder.recordPreventedConflicts(request.resourceIds(), authentication.getName());
            throw new DuplicateKeyException("O horário já está ocupado por uma solicitação concorrente.", exception);
        }
    }

    @PutMapping("/{id}")
    public Reservation update(
            @PathVariable String id,
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication) {
        try {
            return reservationService.update(id, request, authentication.getName(), isAdministrator(authentication));
        } catch (DuplicateKeyException exception) {
            auditRecorder.recordPreventedConflicts(request.resourceIds(), authentication.getName());
            throw exception;
        } catch (UncategorizedMongoDbException exception) {
            if (!isTransientTransactionConflict(exception)) {
                throw exception;
            }
            auditRecorder.recordPreventedConflicts(request.resourceIds(), authentication.getName());
            throw new DuplicateKeyException("O horário já está ocupado por uma solicitação concorrente.", exception);
        }
    }

    @DeleteMapping("/{id}")
    public Reservation cancel(@PathVariable String id, Authentication authentication) {
        return reservationService.cancel(id, authentication.getName(), isAdministrator(authentication));
    }

    @PostMapping("/{id}/approve")
    public Reservation approve(
            @PathVariable String id,
            @RequestParam(required = false) String note,
            Authentication authentication) {
        return reservationService.decide(id, true, note, authentication.getName());
    }

    @PostMapping("/{id}/reject")
    public Reservation reject(
            @PathVariable String id,
            @RequestParam(required = false) String note,
            Authentication authentication) {
        return reservationService.decide(id, false, note, authentication.getName());
    }

    private boolean isAdministrator(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private boolean isTransientTransactionConflict(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof MongoException mongoException
                    && mongoException.hasErrorLabel("TransientTransactionError")) {
                return true;
            }
        }
        return false;
    }
}
