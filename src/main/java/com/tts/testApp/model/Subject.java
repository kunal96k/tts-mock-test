package com.tts.testApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Subject {

    private Long id;
    private String name;
    private int totalQuestions;
    private int studentsEnrolled;

}
