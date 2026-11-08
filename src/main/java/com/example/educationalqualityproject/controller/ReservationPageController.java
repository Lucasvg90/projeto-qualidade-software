package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.dto.ReservationForm;
import com.example.educationalqualityproject.dto.ReservationRequest;
import com.example.educationalqualityproject.entity.Reservation;
import com.example.educationalqualityproject.repository.EducationalResourceRepository;
import com.example.educationalqualityproject.repository.TeacherRepository;
import com.example.educationalqualityproject.service.ReservationService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZoneId;
import java.util.List;

@Controller
@RequestMapping("/reservations")
public class ReservationPageController {

    private final ReservationService reservationService;
    private final EducationalResourceRepository resourceRepository;
    private final TeacherRepository teacherRepository;

    public ReservationPageController(
            ReservationService reservationService,
            EducationalResourceRepository resourceRepository,
            TeacherRepository teacherRepository) {
        this.reservationService = reservationService;
        this.resourceRepository = resourceRepository;
        this.teacherRepository = teacherRepository;
    }

    @GetMapping
    public String list(Authentication authentication, Model model) {
        model.addAttribute("reservations", reservationService.getForRequester(authentication.getName()));
        model.addAttribute("resources", resourceRepository.findAll());
        model.addAttribute("teachers", teacherRepository.findAll());
        model.addAttribute("reservationForm", new ReservationForm());
        return "reservation/list";
    }

    @PostMapping
    public String create(
            @ModelAttribute ReservationForm form,
            Authentication authentication,
            Model model) {
        try {
            reservationService.create(toRequest(form), authentication.getName());
            return "redirect:/reservations";
        } catch (ResponseStatusException | DuplicateKeyException exception) {
            model.addAttribute("error", exception instanceof ResponseStatusException statusException
                    ? statusException.getReason() : "Horário indisponível: outro pedido foi salvo antes.");
            model.addAttribute("reservations", reservationService.getForRequester(authentication.getName()));
            model.addAttribute("resources", resourceRepository.findAll());
            model.addAttribute("teachers", teacherRepository.findAll());
            model.addAttribute("reservationForm", form);
            return "reservation/list";
        }
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable String id, Authentication authentication) {
        reservationService.cancel(id, authentication.getName(), false);
        return "redirect:/reservations";
    }

    private ReservationRequest toRequest(ReservationForm form) {
        if (form.getStartAt() == null || form.getEndAt() == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST,
                    "Informe o início e o fim da reserva.");
        }
        ZoneId zone = ZoneId.systemDefault();
        return new ReservationRequest(form.getResourceIds(), form.getTeacherId(),
                form.getStartAt().atZone(zone).toInstant(),
                form.getEndAt().atZone(zone).toInstant());
    }
}
