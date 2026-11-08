package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.entity.EducationalResource;
import com.example.educationalqualityproject.entity.ResourceType;
import com.example.educationalqualityproject.service.EducationalResourceService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Controller
@RequestMapping("/resources")
public class ResourcePageController {

    private final EducationalResourceService resourceService;

    public ResourcePageController(EducationalResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String competence,
            @RequestParam(required = false) String availableFrom,
            @RequestParam(required = false) String availableUntil,
            Model model) {
        Instant from = parseDateTime(availableFrom);
        Instant until = parseDateTime(availableUntil);
        model.addAttribute("resources", resourceService.search(
                type, capacity, location, competence, from, until));
        model.addAttribute("types", ResourceType.values());
        model.addAttribute("filters", new SearchFilters(type, capacity, location, competence));
        return "resource/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("resource", new EducationalResource());
        model.addAttribute("types", ResourceType.values());
        return "resource/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, Model model) {
        model.addAttribute("resource", resourceService.getById(id));
        model.addAttribute("types", ResourceType.values());
        return "resource/form";
    }

    @PostMapping
    public String create(@ModelAttribute EducationalResource resource) {
        resourceService.save(resource);
        return "redirect:/resources";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable String id, @ModelAttribute EducationalResource resource) {
        resourceService.getById(id);
        resource.setId(id);
        resourceService.save(resource);
        return "redirect:/resources";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id) {
        resourceService.delete(id);
        return "redirect:/resources";
    }

    public record SearchFilters(
            ResourceType type,
            Integer capacity,
            String location,
            String competence) {
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
}
