package com.tts.testApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for answer review
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerReviewDTO {
    private Long questionId;
    private String questionText;
    private String userAnswer;
    private String correctAnswer;
    private String status; // "correct", "incorrect", "unanswered"
    private String explanation;
}
