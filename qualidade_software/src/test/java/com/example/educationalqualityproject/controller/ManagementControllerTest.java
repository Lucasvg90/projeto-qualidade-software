package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.entity.Student;
import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.service.StudentService;
import com.example.educationalqualityproject.service.TeacherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({StudentController.class, TeacherController.class})
class ManagementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StudentService studentService;

    @MockBean
    private TeacherService teacherService;

    @Test
    void shouldRenderStudentsFromService() throws Exception {
        when(studentService.getAllStudents())
                .thenReturn(List.of(new Student("Ana Lima", "ana@example.com", "2026001")));

        mockMvc.perform(get("/students"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Estudantes")))
                .andExpect(content().string(containsString("ana@example.com")))
                .andExpect(content().string(containsString("/students/new")));

        verify(studentService).getAllStudents();
    }

    @Test
    void shouldRenderTeacherEmptyState() throws Exception {
        when(teacherService.getAllTeachers()).thenReturn(List.of());

        mockMvc.perform(get("/teachers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Nenhum professor cadastrado")))
                .andExpect(content().string(containsString("/teachers/new")));

        verify(teacherService).getAllTeachers();
    }

    @Test
    void shouldRenderManagementPagesWhenDatabaseIsUnavailable() throws Exception {
        when(studentService.getAllStudents())
                .thenThrow(new DataAccessResourceFailureException("MongoDB unavailable"));
        when(teacherService.getAllTeachers())
                .thenThrow(new DataAccessResourceFailureException("MongoDB unavailable"));

        mockMvc.perform(get("/students"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Banco de dados indisponível")))
                .andExpect(content().string(containsString("Verifique a conexão com o MongoDB")));

        mockMvc.perform(get("/teachers"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Banco de dados indisponível")))
                .andExpect(content().string(containsString("Verifique a conexão com o MongoDB")));
    }

    @Test
    void shouldRenderStudentAndTeacherForms() throws Exception {
        mockMvc.perform(get("/students/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cadastrar estudante")))
                .andExpect(content().string(containsString("name=\"registrationNumber\"")));

        mockMvc.perform(get("/teachers/new"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Cadastrar professor")))
                .andExpect(content().string(containsString("name=\"department\"")));
    }

    @Test
    void shouldSaveNewStudentAndTeacher() throws Exception {
        mockMvc.perform(post("/students")
                        .param("name", "Ana Lima")
                        .param("email", "ana@example.com")
                        .param("registrationNumber", "2026001"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/students"));

        mockMvc.perform(post("/teachers")
                        .param("name", "João Souza")
                        .param("email", "joao@example.com")
                        .param("department", "Matemática"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/teachers"));

        verify(studentService).saveStudent(any(Student.class));
        verify(teacherService).saveTeacher(any(Teacher.class));
    }
}
