package com.tts.testApp.exception;

public class ResourceNotFoundException  extends RuntimeException{
        public ResourceNotFoundException(String message) {
            super(message);
        }
}
