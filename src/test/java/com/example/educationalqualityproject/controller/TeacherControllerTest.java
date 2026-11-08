package com.example.educationalqualityproject.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TeacherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void requesterCanOpenDedicatedTeacherRegistrationPage() throws Exception {
        mockMvc.perform(get("/teachers/new").with(user("requester@example.com").roles("SOLICITANTE")))
                .andExpect(status().isOk())
                .andExpect(view().name("teacher/form"))
                .andExpect(content().string(containsString("Cadastrar professor")))
                .andExpect(content().string(containsString("Nome completo")));
    }

    @Test
    void anonymousUserMustSignInBeforeOpeningTeacherRegistrationPage() throws Exception {
        mockMvc.perform(get("/teachers/new"))
                .andExpect(status().isUnauthorized());
    }
}
