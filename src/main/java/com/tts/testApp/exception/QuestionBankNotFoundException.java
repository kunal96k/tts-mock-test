
package com.tts.testApp.exception;

public class QuestionBankNotFoundException extends RuntimeException {
    public QuestionBankNotFoundException(String message) {
        super(message);
    }
}