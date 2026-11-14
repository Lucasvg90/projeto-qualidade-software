package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.entity.ReservationStatus;
import com.example.educationalqualityproject.repository.ReservationRepository;
import com.example.educationalqualityproject.repository.TeacherRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final ReservationRepository reservationRepository;
    private final AuditRecorder auditRecorder;

    public TeacherService(
            TeacherRepository teacherRepository,
            ReservationRepository reservationRepository,
            AuditRecorder auditRecorder) {
        this.teacherRepository = teacherRepository;
        this.reservationRepository = reservationRepository;
        this.auditRecorder = auditRecorder;
    }

    public List<Teacher> getAllTeachers() {
        return teacherRepository.findAll();
    }

    public List<Teacher> search(String competence, Instant availableFrom, Instant availableUntil) {
        if ((availableFrom == null) != (availableUntil == null)) {
            throw new ResponseStatusException(BAD_REQUEST, "Informe início e fim da disponibilidade.");
        }
        if (availableFrom != null && !availableFrom.isBefore(availableUntil)) {
            throw new ResponseStatusException(BAD_REQUEST, "O início deve ser anterior ao fim.");
        }

        Set<String> unavailableTeacherIds = availableFrom == null ? Set.of()
                : reservationRepository.findByStatusInAndStartAtLessThanAndEndAtGreaterThan(
                                List.of(ReservationStatus.PENDING_APPROVAL, ReservationStatus.APPROVED),
                                availableUntil, availableFrom)
                        .stream()
                        .map(reservation -> reservation.getTeacherId())
                        .filter(id -> id != null)
                        .collect(Collectors.toSet());

        return teacherRepository.findAll().stream()
                .filter(teacher -> competence == null || teacher.getCompetencies() != null
                        && teacher.getCompetencies().stream()
                        .anyMatch(value -> value.toLowerCase(Locale.ROOT)
                                .contains(competence.toLowerCase(Locale.ROOT))))
                .filter(teacher -> availableFrom == null || !unavailableTeacherIds.contains(teacher.getId()))
                .toList();
    }

    public Optional<Teacher> getTeacherById(String id) {
        return teacherRepository.findById(id);
    }

    @Transactional
    public Teacher saveTeacher(Teacher teacher, String actor) {
        Teacher previous = teacher.getId() == null
                ? null : teacherRepository.findById(teacher.getId()).orElse(null);
        Teacher saved = teacherRepository.save(teacher);
        String action = previous == null ? "TEACHER_CREATED" : "TEACHER_UPDATED";
        String details = previous == null
                ? "Depois: " + describe(saved)
                : "Antes: " + describe(previous) + "; depois: " + describe(saved);
        auditRecorder.record("TEACHER", saved.getId(), action, actor, details);
        return saved;
    }

    @Transactional
    public void deleteTeacher(String id, String actor) {
        Teacher teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Professor não encontrado."));
        teacherRepository.deleteById(id);
        auditRecorder.record("TEACHER", id, "TEACHER_DELETED", actor, "Excluído: " + describe(teacher));
    }

    public boolean existsByEmail(String email) {
        return teacherRepository.existsByEmail(email);
    }

    private String describe(Teacher teacher) {
        return "nome=" + teacher.getName()
                + ", email=" + teacher.getEmail()
                + ", departamento=" + teacher.getDepartment()
                + ", competências=" + teacher.getCompetencies();
    }
}