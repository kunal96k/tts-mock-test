package com.tts.testApp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@Getter
@AllArgsConstructor
public class CreateTestDTO {

    private Long id;

    /**
     * Subject name selected by user
     * Cannot be null
     */
    @NotBlank(message = "Subject is required")
    private String subjectName;

    /**
     * Subject ID (for database relationship)
     */
    private Long subjectId;

    /**
     * Test name (auto-generated from subjectName + testType)
     */
    private String testName;

    /**
     * Type of test: MOCK or FINAL
     */
    @NotBlank(message = "Test type is required")
    @Pattern(regexp = "^(MOCK|FINAL)$", message = "Test type must be either MOCK or FINAL")
    private String testType;

    @NotNull(message = "Total questions is required")
    @Min(value = 1, message = "Total questions must be at least 1")
    @Max(value = 500, message = "Total questions cannot exceed 500")
    private Integer totalQuestions;

    @NotNull(message = "Duration is required")
    @Min(value = 5, message = "Duration must be at least 5 minutes")
    @Max(value = 300, message = "Duration cannot exceed 300 minutes")
    private Integer duration;

    @NotNull(message = "Passing percentage is required")
    @Min(value = 1, message = "Passing percentage must be at least 1")
    @Max(value = 100, message = "Passing percentage cannot exceed 100")
    private Integer passingPercentage;

    @NotNull(message = "Marks per question is required")
    @Min(value = 1, message = "Marks per question must be at least 1")
    @Max(value = 10, message = "Marks per question cannot exceed 10")
    private Integer marksPerQuestion;

    @NotNull(message = "Tab switch limit is required")
    @Min(value = 0, message = "Tab switch limit cannot be negative")
    @Max(value = 50, message = "Tab switch limit cannot exceed 50")
    private Integer tabSwitchLimit;

    /**
     * Calculated total marks for the test
     */
    private Integer totalMarks;

    /**
     * Test creation timestamp
     */
    private LocalDateTime createdAt;

    /**
     * Test update timestamp
     */
    private LocalDateTime updatedAt;

    /**
     * Whether the test is active
     */
    private Boolean active = true;

    /**
     * Calculate total marks
     */
    public Integer calculateTotalMarks() {
        if (totalQuestions != null && marksPerQuestion != null) {
            this.totalMarks = totalQuestions * marksPerQuestion;
            return this.totalMarks;
        }
        return 0;
    }

    /**
     * Calculate minimum passing marks
     */
    public Integer calculatePassingMarks() {
        if (totalMarks != null && passingPercentage != null) {
            return (int) Math.ceil((totalMarks * passingPercentage) / 100.0);
        }
        return 0;
    }

    /**
     * Generate testName from subjectName + testType
     */
    public String generateTestName() {
        if (subjectName != null && testType != null) {
            return subjectName + ("MOCK".equalsIgnoreCase(testType) ? " Mock Test" : " Final Test");
        }
        return null;
    }

    /**
     * Get formatted created date for display
     */
    public String getFormattedCreatedDate() {
        if (createdAt != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
            return createdAt.format(formatter);
        }
        return "N/A";
    }

    /**
     * Get formatted updated date for display
     */
    public String getFormattedUpdatedDate() {
        if (updatedAt != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a");
            return updatedAt.format(formatter);
        }
        return "N/A";
    }
}