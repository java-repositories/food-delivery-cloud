package ru.fooddelivery.aggregate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DishNotFoundException.class)
    public ProblemDetail handleNotFound(DishNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle("Блюдо не найдено");
        return problem;
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ProblemDetail handleExternalError(ExternalServiceException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage());
        problem.setTitle("Внешний сервис временно недоступен");
        return problem;
    }
}
