package com.example.educationalqualityproject.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebErrorControllerAdviceTest {

    @Test
    void showsTheBusinessMessageAndHttpStatusInTheErrorView() {
        var result = new WebErrorControllerAdvice().handleStatus(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "O recurso está em manutenção."));

        assertEquals("error", result.getViewName());
        assertEquals(400, result.getModel().get("status"));
        assertEquals("O recurso está em manutenção.", result.getModel().get("message"));
        assertEquals(HttpStatus.BAD_REQUEST, result.getStatus());
    }
}
