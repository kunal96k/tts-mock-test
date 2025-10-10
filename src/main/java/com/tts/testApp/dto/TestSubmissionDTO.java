package com.tts.testApp.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for test submission
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestSubmissionDTO {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Test ID is required")
    private Long testId;

    @NotNull(message = "Question bank ID is required")
    private Long questionBankId;

    @NotNull(message = "Answers are required")
    private List<StudentAnswerDTO> answers;

    @Min(value = 0, message = "Time taken cannot be negative")
    private int timeTakenSeconds;

    private int tabSwitches;
}
