package com.tts.testApp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "test_attempts", indexes = {
        @Index(name = "idx_student_id", columnList = "student_id"),
        @Index(name = "idx_test_id", columnList = "test_id"),
        @Index(name = "idx_attempt_date", columnList = "attempt_date")
})
public class TestAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false)
    private Long testId;

    @Column(nullable = false)
    private Long questionBankId;

    @Column(nullable = false)
    private int totalQuestions;

    @Column(nullable = false)
    private int correctAnswers;

    @Column(nullable = false)
    private int wrongAnswers;

    @Column(nullable = false)
    private int unanswered;

    @Column(nullable = false)
    private int totalMarks;

    @Column(nullable = false)
    private int obtainedMarks;

    @Column(nullable = false)
    private double scorePercentage;

    @Column(length = 5)
    private String grade;

    @Column(nullable = false)
    private boolean passed;

    @Column(nullable = false)
    private int timeTakenSeconds;

    @Column(nullable = false)
    private int tabSwitches;

    @Column(nullable = false, length = 100)
    private String username;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime attemptDate;

    @Column(columnDefinition = "TEXT")
    private String remarks;

    // Helper methods
    public String getFormattedTime() {
        int minutes = timeTakenSeconds / 60;
        int seconds = timeTakenSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public String getStatus() {
        return passed ? "PASSED" : "FAILED";
    }
}