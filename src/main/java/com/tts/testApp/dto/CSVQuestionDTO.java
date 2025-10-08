package com.tts.testApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for parsing CSV rows
 * Expected CSV format:
 * Question,Option A,Option B,Option C,Option D,Correct Answer,Explanation,Marks,Difficulty
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CSVQuestionDTO {

    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private String explanation;
    private Integer marks;
    private String difficultyLevel;

    // Validation method
    public boolean isValid() {
        return questionText != null && !questionText.trim().isEmpty() &&
                optionA != null && !optionA.trim().isEmpty() &&
                optionB != null && !optionB.trim().isEmpty() &&
                optionC != null && !optionC.trim().isEmpty() &&
                optionD != null && !optionD.trim().isEmpty() &&
                correctAnswer != null && correctAnswer.matches("[A-Da-d]");
    }

    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();

        if (questionText == null || questionText.trim().isEmpty()) {
            errors.append("Question text is required. ");
        }
        if (optionA == null || optionA.trim().isEmpty()) {
            errors.append("Option A is required. ");
        }
        if (optionB == null || optionB.trim().isEmpty()) {
            errors.append("Option B is required. ");
        }
        if (optionC == null || optionC.trim().isEmpty()) {
            errors.append("Option C is required. ");
        }
        if (optionD == null || optionD.trim().isEmpty()) {
            errors.append("Option D is required. ");
        }
        if (correctAnswer == null || !correctAnswer.matches("[A-Da-d]")) {
            errors.append("Correct answer must be A, B, C, or D. ");
        }

        return errors.toString();
    }

    // Normalize data
    public void normalize() {
        if (correctAnswer != null) {
            correctAnswer = correctAnswer.toUpperCase().trim();
        }
        if (difficultyLevel != null) {
            difficultyLevel = difficultyLevel.toUpperCase().trim();
            if (!difficultyLevel.matches("EASY|MEDIUM|HARD")) {
                difficultyLevel = "MEDIUM";
            }
        } else {
            difficultyLevel = "MEDIUM";
        }
        if (marks == null || marks < 1) {
            marks = 1;
        }
        if (marks > 10) {
            marks = 10;
        }
    }
}