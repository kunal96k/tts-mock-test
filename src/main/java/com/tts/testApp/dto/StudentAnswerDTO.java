package com.tts.testApp.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for student answer submission
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentAnswerDTO {
    @NotNull(message = "Question ID is required")
    private Long questionId;

    @Pattern(regexp = "^[A-D]$|^$", message = "Answer must be A, B, C, or D")
    private String selectedOption; // Can be null if unanswered
}