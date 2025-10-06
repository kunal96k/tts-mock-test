package com.tts.testApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SubjectPerformance {

    private String subjectName;
    private int totalTests;
    private int avgScore;
    private int passRate;
    private int avgTime;


}
