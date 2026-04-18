package com.SpringCrudApp.crudApp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class ExcelProcessingException extends RuntimeException{

    public ExcelProcessingException(String message) {
        super(message);
    }
}
