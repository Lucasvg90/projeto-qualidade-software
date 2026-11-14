package com.example.educationalqualityproject.controller.api;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ProblemDetail handleConflict(DuplicateKeyException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "O horário ou registro solicitado já está ocupado.");
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleStatusException(ResponseStatusException exception) {
        return ProblemDetail.forStatusAndDetail(exception.getStatusCode(), exception.getReason());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
                "Você não tem permissão para realizar esta operação.");
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ProblemDetail handleInvalidRequest() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "Confira os dados enviados e tente novamente.");
    }
}
