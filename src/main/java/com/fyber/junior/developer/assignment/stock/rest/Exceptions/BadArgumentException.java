package com.fyber.junior.developer.assignment.stock.rest.Exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class BadArgumentException extends RuntimeException{
        public BadArgumentException(String message){ super(message);}
}
