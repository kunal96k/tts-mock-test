package com.tts.testApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStats {

    private int totalStudents;
    private int testsCompleted;
    private int passRate;
    private int activeSubjects;
}
