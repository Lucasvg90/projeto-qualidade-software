package com.example.educationalqualityproject.controller.api;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ProblemDetail handleReservationConflict(DuplicateKeyException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "O horário solicitado já foi reservado por outro usuário.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleStatusException(ResponseStatusException exception) {
        return ProblemDetail.forStatusAndDetail(exception.getStatusCode(), exception.getReason());
    }
}
