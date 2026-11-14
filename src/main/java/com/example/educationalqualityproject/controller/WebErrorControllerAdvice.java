package com.example.educationalqualityproject.controller;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

@ControllerAdvice(assignableTypes = {
        AuthController.class,
        HomeController.class,
        ResourcePageController.class,
        ReservationPageController.class,
        AdminReservationController.class,
        TeacherController.class,
        StudentController.class
})
public class WebErrorControllerAdvice {

    @ExceptionHandler(ResponseStatusException.class)
    public ModelAndView handleStatus(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String message = exception.getReason() == null ? "Não foi possível concluir a operação." : exception.getReason();
        return error(status, message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ModelAndView handleAccessDenied() {
        return error(HttpStatus.FORBIDDEN, "Você não tem permissão para realizar esta operação.");
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public ModelAndView handleConflict() {
        return error(HttpStatus.CONFLICT, "Este horário ou registro já está ocupado.");
    }

    @ExceptionHandler({
            BindException.class,
            MethodArgumentNotValidException.class,
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class
    })
    public ModelAndView handleInvalidInput() {
        return error(HttpStatus.BAD_REQUEST, "Confira os dados informados e tente novamente.");
    }

    private ModelAndView error(HttpStatus status, String message) {
        return new ModelAndView("error", Map.of(
                "status", status.value(),
                "message", message), status);
    }
}
