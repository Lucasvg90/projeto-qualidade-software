package com.example.educationalqualityproject.controller;

import com.example.educationalqualityproject.entity.Teacher;
import com.example.educationalqualityproject.service.TeacherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/teachers")
public class TeacherController {

    private static final Logger logger = LoggerFactory.getLogger(TeacherController.class);

    @Autowired
    private TeacherService teacherService;

    @GetMapping
    public String listTeachers(Model model) {
        List<Teacher> teachers;
        try {
            teachers = teacherService.getAllTeachers();
        } catch (DataAccessException exception) {
            logger.error("Não foi possível carregar professores do MongoDB", exception);
            teachers = List.of();
            model.addAttribute("databaseError",
                    "Não foi possível acessar o banco de dados. Verifique a conexão com o MongoDB e tente novamente.");
        }
        model.addAttribute("teachers", teachers);
        return "teacher/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("teacher", new Teacher());
        return "teacher/form";
    }

    @PostMapping
    public String createTeacher(@ModelAttribute Teacher teacher) {
        teacherService.saveTeacher(teacher);
        return "redirect:/teachers";
    }

    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable String id, Model model) {
        Teacher teacher = teacherService.getTeacherById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid teacher Id:" + id));
        model.addAttribute("teacher", teacher);
        return "teacher/form";
    }

    @PostMapping("/{id}")
    public String updateTeacher(@PathVariable String id, @ModelAttribute Teacher teacher) {
        teacher.setId(id);
        teacherService.saveTeacher(teacher);
        return "redirect:/teachers";
    }

    @GetMapping("/{id}/delete")
    public String deleteTeacher(@PathVariable String id) {
        teacherService.deleteTeacher(id);
        return "redirect:/teachers";
    }
}