package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.service.TeacherService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Controller
@RequestMapping("/teachers")
public class TeacherController {

    private final TeacherService teacherService;

    public TeacherController(TeacherService teacherService) {
        this.teacherService = teacherService;
    }

    @GetMapping
    public String listTeachers(
            @RequestParam(required = false) String competence,
            @RequestParam(required = false) String availableFrom,
            @RequestParam(required = false) String availableUntil,
            Model model) {
        List<Teacher> teachers = teacherService.search(
                competence, parseDateTime(availableFrom), parseDateTime(availableUntil));
        model.addAttribute("teachers", teachers);
        model.addAttribute("competence", competence);
        return "teacher/list";
    }

    private Instant parseDateTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Instant.parse(value);
        } catch (java.time.format.DateTimeParseException ignored) {
            try {
                return LocalDateTime.parse(value).atZone(ZoneId.systemDefault()).toInstant();
            } catch (java.time.format.DateTimeParseException exception) {
                throw new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.BAD_REQUEST,
                        "Informe uma data e hora válidas para a disponibilidade.", exception);
            }
        }
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("teacher", new Teacher());
        return "teacher/form";
    }

    @PostMapping
    public String createTeacher(@ModelAttribute Teacher teacher, Authentication authentication) {
        teacherService.saveTeacher(teacher, authentication.getName());
        return "redirect:/teachers";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable String id, Model model) {
        Teacher teacher = teacherService.getTeacherById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Professor não encontrado."));
        model.addAttribute("teacher", teacher);
        return "teacher/form";
    }

    @PostMapping("/{id}")
    public String updateTeacher(
            @PathVariable String id,
            @ModelAttribute Teacher teacher,
            Authentication authentication) {
        teacher.setId(id);
        teacherService.saveTeacher(teacher, authentication.getName());
        return "redirect:/teachers";
    }

    @GetMapping("/{id}/delete")
    public String deleteTeacher(@PathVariable String id, Authentication authentication) {
        teacherService.deleteTeacher(id, authentication.getName());
        return "redirect:/teachers";
    }
}