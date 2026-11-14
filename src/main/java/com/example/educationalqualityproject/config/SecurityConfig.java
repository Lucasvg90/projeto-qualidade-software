package com.example.educationalqualityproject.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.nio.charset.StandardCharsets;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ObjectMapper objectMapper) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/login", "/register", "/error").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/resources/**", "/api/teachers/**").authenticated()
                        .requestMatchers("/api/resources/**", "/api/teachers/**", "/api/students/**")
                        .hasRole("ADMIN")
                        .requestMatchers("/api/reservations/*/approve", "/api/reservations/*/reject",
                                "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/resources/new", "/resources/*/edit").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/resources/**").hasRole("ADMIN")
                        .requestMatchers("/api/reservations/**", "/reservations/**", "/resources/**")
                        .authenticated()
                        .requestMatchers(HttpMethod.GET, "/teachers").authenticated()
                        .requestMatchers(HttpMethod.GET, "/teachers/new").authenticated()
                        .requestMatchers(HttpMethod.GET, "/teachers/*/edit", "/teachers/*/delete").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/teachers").authenticated()
                        .requestMatchers("/teachers/**", "/students/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/"))
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                apiAuthenticationEntryPoint(objectMapper),
                                new AntPathRequestMatcher("/api/**"))
                        .accessDeniedHandler(accessDeniedHandler(objectMapper)));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private AuthenticationEntryPoint apiAuthenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, exception) -> writeProblem(
                objectMapper, response, HttpStatus.UNAUTHORIZED,
                "Faça login para acessar este recurso.");
    }

    private AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, exception) -> {
            if (request.getRequestURI().startsWith("/api/")) {
                writeProblem(objectMapper, response, HttpStatus.FORBIDDEN,
                        "Você não tem permissão para realizar esta operação.");
            } else {
                response.sendError(HttpStatus.FORBIDDEN.value(),
                        "Você não tem permissão para realizar esta operação.");
            }
        };
    }

    private void writeProblem(
            ObjectMapper objectMapper,
            jakarta.servlet.http.HttpServletResponse response,
            HttpStatus status,
            String detail) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(response.getOutputStream(), ProblemDetail.forStatusAndDetail(status, detail));
    }
}
