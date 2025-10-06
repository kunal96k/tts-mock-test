package com.tts.testApp.exception;

// AdminAlreadyExistsException.java
public class AdminAlreadyExistsException extends RuntimeException {
    public AdminAlreadyExistsException(String message) {
        super(message);
    }
}
