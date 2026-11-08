package com.example.educationalqualityproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
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
                                new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED),
                                new AntPathRequestMatcher("/api/**")));
        return http.build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
