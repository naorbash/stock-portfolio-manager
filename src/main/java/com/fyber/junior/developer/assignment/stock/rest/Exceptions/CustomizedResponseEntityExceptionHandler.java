package com.fyber.junior.developer.assignment.stock.rest.Exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;

@ControllerAdvice
@RestController
public class CustomizedResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleAllException(Exception ex, WebRequest request){
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(new Date(),ex.getMessage(),request.getDescription(true));

        return new ResponseEntity(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public final ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request){
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(new Date(),ex.getMessage(),request.getDescription(true));

        return new ResponseEntity(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadArgumentException.class)
    public final ResponseEntity<Object> handleBagArgumentException(BadArgumentException ex, WebRequest request){
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(new Date(),ex.getMessage(),request.getDescription(true));

        return new ResponseEntity(exceptionResponse, HttpStatus.NOT_ACCEPTABLE);
    }

    @ExceptionHandler(ConflictException.class)
    public final ResponseEntity<Object> handleConflictException(ConflictException ex, WebRequest request){
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(new Date(),ex.getMessage(),request.getDescription(true));

        return new ResponseEntity(exceptionResponse, HttpStatus.CONFLICT);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        ExceptionResponse exceptionResponse =
                new ExceptionResponse(new Date(),"Validation Failed",ex.getBindingResult().toString());
        return new ResponseEntity(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

}
