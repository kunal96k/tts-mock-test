package com.tts.testApp.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "tests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Foreign key relationship to Subject table
     * Use @ManyToOne instead of storing subject name as String
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id", nullable = false, referencedColumnName = "id")
    private Subject subject;

    /**
     * The display name of the test (subject + type)
     * Example: "Core Java Mock Test"
     */
    @Column(nullable = false, unique = true, length = 200)
    private String testName;

    /**
     * Type of test: MOCK or FINAL
     */
    @Column(nullable = false, length = 20)
    private String testType;

    @Column(nullable = false)
    private Integer totalQuestions;

    @Column(nullable = false)
    private Integer duration; // in minutes

    @Column(nullable = false)
    private Integer passingPercentage;

    @Column(nullable = false)
    private Integer marksPerQuestion;

    @Column(nullable = false)
    private Integer totalMarks;

    @Column(nullable = false)
    private Integer tabSwitchLimit;

    @Column(name = "question_bank_id")
    private Long questionBankId;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;

        // Automatically derive testName if subject and type are provided
        if (subject != null && subject.getName() != null && testType != null) {
            String suffix = "MOCK".equalsIgnoreCase(testType) ? " Mock Test" : " Final Test";
            this.testName = subject.getName() + suffix;
        }

        // Calculate total marks if not set
        if (totalMarks == null && totalQuestions != null && marksPerQuestion != null) {
            this.totalMarks = totalQuestions * marksPerQuestion;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();

        // Keep testName synced with subject and type if either changes
        if (subject != null && subject.getName() != null && testType != null) {
            String suffix = "MOCK".equalsIgnoreCase(testType) ? " Mock Test" : " Final Test";
            this.testName = subject.getName() + suffix;
        }
    }

    /**
     * Helper method to get subject name
     */
    public String getSubjectName() {
        return subject != null ? subject.getName() : null;
    }

    /**
     * Helper method to set subject by name (for backward compatibility)
     */
    public void setSubjectByName(String subjectName) {
        if (this.subject == null) {
            this.subject = new Subject();
        }
        this.subject.setName(subjectName);
    }
}