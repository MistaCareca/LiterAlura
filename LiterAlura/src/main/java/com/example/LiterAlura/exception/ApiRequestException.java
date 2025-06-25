package com.example.LiterAlura.exception;

public class ApiRequestException extends LiterAluraException {
    public ApiRequestException(String message) {
        super(message);
    }

    public ApiRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}