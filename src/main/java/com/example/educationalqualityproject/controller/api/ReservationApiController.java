package com.example.educationalqualityproject.controller.api;

import com.example.educationalqualityproject.dto.ReservationRequest;
import com.example.educationalqualityproject.entity.Reservation;
import com.example.educationalqualityproject.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationApiController {

    private final ReservationService reservationService;

    public ReservationApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
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
        return reservationService.create(request, authentication.getName());
    }

    @PutMapping("/{id}")
    public Reservation update(
            @PathVariable String id,
            @Valid @RequestBody ReservationRequest request,
            Authentication authentication) {
        return reservationService.update(id, request, authentication.getName(), isAdministrator(authentication));
    }

    @DeleteMapping("/{id}")
    public Reservation cancel(@PathVariable String id, Authentication authentication) {
        return reservationService.cancel(id, authentication.getName(), isAdministrator(authentication));
    }

    @PostMapping("/{id}/approve")
    public Reservation approve(@PathVariable String id, @RequestParam(required = false) String note) {
        return reservationService.decide(id, true, note);
    }

    @PostMapping("/{id}/reject")
    public Reservation reject(@PathVariable String id, @RequestParam(required = false) String note) {
        return reservationService.decide(id, false, note);
    }

    private boolean isAdministrator(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }
}
