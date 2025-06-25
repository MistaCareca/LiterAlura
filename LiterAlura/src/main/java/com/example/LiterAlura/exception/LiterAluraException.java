package com.example.LiterAlura.exception;

public class LiterAluraException extends RuntimeException {
    public LiterAluraException(String message) {
        super(message);
    }

    public LiterAluraException(String message, Throwable cause) {
        super(message, cause);
    }
}