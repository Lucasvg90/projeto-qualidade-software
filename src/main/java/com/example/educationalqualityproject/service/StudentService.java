package com.example.educationalqualityproject.service;

import com.example.educationalqualityproject.entity.Student;
import com.example.educationalqualityproject.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final AuditRecorder auditRecorder;

    public StudentService(StudentRepository studentRepository, AuditRecorder auditRecorder) {
        this.studentRepository = studentRepository;
        this.auditRecorder = auditRecorder;
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentById(String id) {
        return studentRepository.findById(id);
    }

    @Transactional
    public Student saveStudent(Student student, String actor) {
        Student previous = student.getId() == null
                ? null : studentRepository.findById(student.getId()).orElse(null);
        Student saved = studentRepository.save(student);
        String action = previous == null ? "STUDENT_CREATED" : "STUDENT_UPDATED";
        String details = previous == null
                ? "Depois: " + describe(saved)
                : "Antes: " + describe(previous) + "; depois: " + describe(saved);
        auditRecorder.record("STUDENT", saved.getId(), action, actor, details);
        return saved;
    }

    @Transactional
    public void deleteStudent(String id, String actor) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Estudante não encontrado."));
        studentRepository.deleteById(id);
        auditRecorder.record("STUDENT", id, "STUDENT_DELETED", actor, "Excluído: " + describe(student));
    }

    public boolean existsByEmail(String email) {
        return studentRepository.existsByEmail(email);
    }

    public boolean existsByRegistrationNumber(String registrationNumber) {
        return studentRepository.existsByRegistrationNumber(registrationNumber);
    }

    private String describe(Student student) {
        return "nome=" + student.getName()
                + ", email=" + student.getEmail()
                + ", matrícula=" + student.getRegistrationNumber();
    }
}