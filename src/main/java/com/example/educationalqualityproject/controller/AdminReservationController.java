package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.entity.Reservation;
import com.example.educationalqualityproject.entity.ReservationStatus;
import com.example.educationalqualityproject.service.ReservationService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public String approvals(Model model) {
        model.addAttribute("reservations", reservationService.getAll().stream()
                .filter(reservation -> reservation.getStatus() == ReservationStatus.PENDING_APPROVAL)
                .toList());
        return "reservation/approvals";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable String id, Authentication authentication) {
        reservationService.decide(id, true, null, authentication.getName());
        return "redirect:/admin/reservations";
    }

    @PostMapping("/{id}/reject")
    public String reject(
            @PathVariable String id,
            @RequestParam(required = false) String note,
            Authentication authentication) {
        reservationService.decide(id, false, note, authentication.getName());
        return "redirect:/admin/reservations";
    }
}
