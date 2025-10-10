package com.tts.testApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.util.List;

/**
 * DTO for sending questions to frontend (without correct answer)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDTO {
    private Long id;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private int marks;
    private String difficultyLevel;
    // correctAnswer is intentionally excluded for security
}
