package com.SpringCrudApp.crudApp.exception;

public class InvalidFileFormatException extends RuntimeException{

    public InvalidFileFormatException(String message) {
        super(message);
    }
}
