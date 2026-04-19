package com.SpringCrudApp.crudApp.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice

public class GlobalExceptionHandler {

    private final RestClient.Builder builder;

    public GlobalExceptionHandler(RestClient.Builder builder) {
        this.builder = builder;
    }

    @ExceptionHandler(EmployeeNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEmployeeNotFoundException(EmployeeNotFoundException notFoundException,
                                                                               WebRequest request){
        log.error("Resource Not Found: {}", notFoundException.getMessage());

        Map<String, Object> errorBody = builderErrorBody(
                HttpStatus.NOT_FOUND.value(), "Resource Not Found",
                notFoundException.getMessage(), request.getDescription(false));

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorBody);
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateEmailException(DuplicateEmailException duplicateEmail,
                                                                             WebRequest request){

        log.error("Email already exists: {}", duplicateEmail.getMessage());

        Map<String, Object> errorbody = builderErrorBody(
                HttpStatus.CONFLICT.value(), "Email aleady exists",
                duplicateEmail.getMessage(), request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorbody);

    }

    //@ExceptionHandler()

    private Map<String, Object> builderErrorBody(int status, String error,
                                                 String message, String path)
    {
        Map<String, Object> body = new HashMap<>();
        body.put("timeStamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);

        return body;
    }

}
