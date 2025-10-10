package com.tts.testApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for test result
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestResultDTO {
    private int correctAnswers;
    private int wrongAnswers;
    private int unanswered;
    private int totalMarks;
    private int obtainedMarks;
    private double scorePercentage;
    private String grade;
    private boolean passed;
    private List<AnswerReviewDTO> reviewData;

    public String calculateGrade() {
        if (scorePercentage >= 90) return "A+";
        if (scorePercentage >= 80) return "A";
        if (scorePercentage >= 70) return "B";
        if (scorePercentage >= 60) return "C";
        if (scorePercentage >= 50) return "D";
        if (scorePercentage >= 35) return "E";
        return "F";
    }
}
