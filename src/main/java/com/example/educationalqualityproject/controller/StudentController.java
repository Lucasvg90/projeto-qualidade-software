package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.entity.Student;
import com.example.educationalqualityproject.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentController {

    @Autowired
    private StudentService studentService;

    @GetMapping
    public String listStudents(Model model) {
        List<Student> students = studentService.getAllStudents();
        model.addAttribute("students", students);
        return "student/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("student", new Student());
        return "student/form";
    }

    @PostMapping
    public String createStudent(@ModelAttribute Student student, Authentication authentication) {
        studentService.saveStudent(student, authentication.getName());
        return "redirect:/students";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable String id, Model model) {
        Student student = studentService.getStudentById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Estudante não encontrado."));
        model.addAttribute("student", student);
        return "student/form";
    }

    @PostMapping("/{id}")
    public String updateStudent(
            @PathVariable String id,
            @ModelAttribute Student student,
            Authentication authentication) {
        student.setId(id);
        studentService.saveStudent(student, authentication.getName());
        return "redirect:/students";
    }

    @GetMapping("/{id}/delete")
    public String deleteStudent(@PathVariable String id, Authentication authentication) {
        studentService.deleteStudent(id, authentication.getName());
        return "redirect:/students";
    }
}