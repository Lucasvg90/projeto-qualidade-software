package com.example.educationalqualityproject.controller.api;

import com.example.educationalqualityproject.entity.EducationalResource;
import com.example.educationalqualityproject.entity.ResourceType;
import com.example.educationalqualityproject.service.EducationalResourceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/resources")
public class EducationalResourceApiController {

    private final EducationalResourceService resourceService;

    public EducationalResourceApiController(EducationalResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @GetMapping
    public List<EducationalResource> search(
            @RequestParam(required = false) ResourceType type,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String competence,
            @RequestParam(required = false) Instant availableFrom,
            @RequestParam(required = false) Instant availableUntil) {
        return resourceService.search(type, capacity, location, competence, availableFrom, availableUntil);
    }

    @GetMapping("/{id}")
    public EducationalResource getById(@PathVariable String id) {
        return resourceService.getById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EducationalResource create(@Valid @RequestBody EducationalResource resource) {
        return resourceService.save(resource);
    }

    @PutMapping("/{id}")
    public EducationalResource update(@PathVariable String id, @Valid @RequestBody EducationalResource resource) {
        resourceService.getById(id);
        resource.setId(id);
        return resourceService.save(resource);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        resourceService.delete(id);
    }
}
