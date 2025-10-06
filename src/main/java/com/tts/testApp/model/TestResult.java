package com.tts.testApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class TestResult {

    private String studentId;
    private String studentName;
    private String subjectName;
    private int score;
    private int totalMarks;
    private boolean passed;
    private LocalDate testDate;
    private Integer timeTaken;
}


