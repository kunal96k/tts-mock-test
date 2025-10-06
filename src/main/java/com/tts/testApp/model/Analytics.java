package com.tts.testApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Analytics {

    private int avgScore;
    private int completionRate;
    private int avgTime;
    private String topPerformer;
    private int topPerformerScore;
    private String scoreChange;
    private String completionChange;
    private String timeChange;

}
