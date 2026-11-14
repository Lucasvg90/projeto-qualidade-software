package com.example.educationalqualityproject.controller.api;

import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.service.TeacherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.time.Instant;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequestMapping("/api/teachers")
@CrossOrigin(origins = "*") // Allow cross-origin requests for external applications
public class TeacherApiController {

    @Autowired
    private TeacherService teacherService;

    /**
     * Get all teachers
     */
    @GetMapping
    public ResponseEntity<List<Teacher>> getAllTeachers(
            @RequestParam(required = false) String competence,
            @RequestParam(required = false) Instant availableFrom,
            @RequestParam(required = false) Instant availableUntil) {
        List<Teacher> teachers = teacherService.search(competence, availableFrom, availableUntil);
        return new ResponseEntity<>(teachers, HttpStatus.OK);
    }

    /**
     * Get teacher by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getTeacherById(@PathVariable String id) {
        Optional<Teacher> teacher = teacherService.getTeacherById(id);
        if (teacher.isPresent()) {
            return new ResponseEntity<>(teacher.get(), HttpStatus.OK);
        } else {
            throw new ResponseStatusException(NOT_FOUND, "Professor não encontrado.");
        }
    }

    /**
     * Create a new teacher
     */
    @PostMapping
    public ResponseEntity<Teacher> createTeacher(@RequestBody Teacher teacher, Authentication authentication) {
        // Check if email already exists
        if (teacherService.existsByEmail(teacher.getEmail())) {
            throw new ResponseStatusException(CONFLICT, "Já existe um professor com este e-mail.");
        }
        
        Teacher savedTeacher = teacherService.saveTeacher(teacher, authentication.getName());
        return new ResponseEntity<>(savedTeacher, HttpStatus.CREATED);
    }

    /**
     * Update an existing teacher
     */
    @PutMapping("/{id}")
    public ResponseEntity<Teacher> updateTeacher(
            @PathVariable String id,
            @RequestBody Teacher teacher,
            Authentication authentication) {
        Optional<Teacher> existingTeacher = teacherService.getTeacherById(id);
        if (!existingTeacher.isPresent()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        // Check if email already exists for other teachers
        if (teacher.getEmail() != null && !teacher.getEmail().equals(existingTeacher.get().getEmail())) {
            if (teacherService.existsByEmail(teacher.getEmail())) {
                throw new ResponseStatusException(CONFLICT, "Já existe um professor com este e-mail.");
            }
        }
        
        teacher.setId(id); // Ensure the ID remains the same
        Teacher updatedTeacher = teacherService.saveTeacher(teacher, authentication.getName());
        return new ResponseEntity<>(updatedTeacher, HttpStatus.OK);
    }

    /**
     * Delete a teacher
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeacher(@PathVariable String id, Authentication authentication) {
        Optional<Teacher> teacher = teacherService.getTeacherById(id);
        if (teacher.isPresent()) {
            teacherService.deleteTeacher(id, authentication.getName());
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            throw new ResponseStatusException(NOT_FOUND, "Professor não encontrado.");
        }
    }
}