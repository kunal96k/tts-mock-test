package com.tts.testApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class QuestionBank {

    private Long id;
    private String subjectName;
    private String fileName;
    private int totalQuestions;
    private LocalDate lastUpdated;
    private boolean active;

}
