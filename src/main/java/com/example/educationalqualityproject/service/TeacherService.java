package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.entity.ReservationStatus;
import com.example.educationalqualityproject.repository.ReservationRepository;
import com.example.educationalqualityproject.repository.TeacherRepository;
import org.springframework.stereotype.Service;
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

    public TeacherService(TeacherRepository teacherRepository, ReservationRepository reservationRepository) {
        this.teacherRepository = teacherRepository;
        this.reservationRepository = reservationRepository;
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

    public Teacher saveTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    public void deleteTeacher(String id) {
        teacherRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return teacherRepository.existsByEmail(email);
    }
}