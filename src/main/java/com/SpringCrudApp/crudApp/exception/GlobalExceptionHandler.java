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

        Map<String, Object> errorBody = builderErrorBody(
                HttpStatus.CONFLICT.value(), "Email already exists",
                duplicateEmail.getMessage(), request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorBody);

    }

    @ExceptionHandler(ExcelProcessingException.class)
    public ResponseEntity<Map<String, Object>> handleExcelProcessingException(
            ExcelProcessingException processingException, WebRequest request){

        log.error("Excel encountered an error while processing: {}", processingException.getMessage());

        Map<String, Object> errorBody = builderErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Excel encountered an error while processing",
                processingException.getMessage(), request.getDescription(false));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);

    }

    @ExceptionHandler(InvalidFileFormatException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidFileFormatException(
            InvalidFileFormatException invalidFileFormatException, WebRequest request){
        log.error("Invalid file format, accepts only .xlsx file: {}", invalidFileFormatException.getMessage());

        Map<String , Object> errorBody = builderErrorBody(HttpStatus.BAD_REQUEST.value(), "Invalid file format, accepts only .xlsx file",
                invalidFileFormatException.getMessage(), request.getDescription(false));

        return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorBody);

    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex,
            WebRequest request) {

        log.error("Unexpected error: ", ex);

        Map<String, Object> errorBody = builderErrorBody(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                request.getDescription(false)
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorBody);
    }


    private Map<String, Object> builderErrorBody(int status, String error,
                                                 String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timeStamp", LocalDateTime.now().toString());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);

        return body;
    }

}
