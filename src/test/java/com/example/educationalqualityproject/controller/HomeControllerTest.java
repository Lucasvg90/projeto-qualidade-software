package com.example.educationalqualityproject.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnHomeView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("Gestão de recursos educacionais")))
                .andExpect(content().string(containsString("name=\"viewport\"")));
    }

    @Test
    void shouldShowAuthenticationLinksOnlyToAnonymousUsers() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/login\"")))
                .andExpect(content().string(containsString("href=\"/register\"")))
                .andExpect(content().string(not(containsString(">Sair</button>"))));

        mockMvc.perform(get("/").with(user("requester@example.com").roles("SOLICITANTE")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("href=\"/login\""))))
                .andExpect(content().string(not(containsString("href=\"/register\""))))
                .andExpect(content().string(containsString(">Sair</button>")));
    }

    @Test
    void shouldRenderLoginAndRegistrationPages() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Entrar")));
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Criar conta de solicitante")));
    }

    @Test
    void apiRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(content().string(containsString("Faça login para acessar este recurso.")));
    }

    @Test
    void adminApiExplainsForbiddenAccessToOtherProfiles() throws Exception {
        mockMvc.perform(get("/api/admin/checkouts")
                        .with(user("requester@example.com").roles("SOLICITANTE")))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(content().string(containsString("Você não tem permissão")));
    }
}