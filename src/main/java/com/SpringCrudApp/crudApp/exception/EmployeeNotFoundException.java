package com.SpringCrudApp.crudApp.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class EmployeeNotFoundException extends RuntimeException{

    public EmployeeNotFoundException(String message){
        super(message);
    }

    public EmployeeNotFoundException(Long id) {
        super("Employee not found with id: " + id);
    }

}
