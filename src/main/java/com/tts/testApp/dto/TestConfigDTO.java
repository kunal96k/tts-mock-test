package com.tts.testApp.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for test configuration
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestConfigDTO {
    private Long questionBankId;
    private String testName;
    private int totalQuestions;
    private int durationMinutes;
    private int passingPercentage;
    private int tabSwitchLimit;

    // Optional: difficulty distribution
    private Integer easyCount;
    private Integer mediumCount;
    private Integer hardCount;
}
