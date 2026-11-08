package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.repository.ReservationRepository;
import com.example.educationalqualityproject.repository.TeacherRepository;
import com.example.educationalqualityproject.service.TeacherService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TeacherRegistrationControllerTest {

    @Test
    void registrationSubmitsTeacherAndReturnsToDirectory() {
        CapturingTeacherService service = new CapturingTeacherService();
        TeacherController controller = new TeacherController(service);
        Teacher teacher = new Teacher("Ana Silva", "ana@example.com", "Ciências");

        String view = controller.createTeacher(teacher);

        assertEquals("redirect:/teachers", view);
        assertEquals(teacher, service.savedTeacher);
    }

    private static class CapturingTeacherService extends TeacherService {

        private Teacher savedTeacher;

        private CapturingTeacherService() {
            super(null, null);
        }

        @Override
        public Teacher saveTeacher(Teacher teacher) {
            savedTeacher = teacher;
            return teacher;
        }
    }
}
