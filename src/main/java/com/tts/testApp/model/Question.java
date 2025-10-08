package com.tts.testApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "questions", indexes = {
        @Index(name = "idx_question_bank_id", columnList = "question_bank_id"),
        @Index(name = "idx_difficulty_level", columnList = "difficultyLevel")
})
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Question text is required")
    @Size(min = 10, max = 1000, message = "Question must be between 10 and 1000 characters")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String questionText;

    @NotBlank(message = "Option A is required")
    @Size(max = 500, message = "Option A must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String optionA;

    @NotBlank(message = "Option B is required")
    @Size(max = 500, message = "Option B must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String optionB;

    @NotBlank(message = "Option C is required")
    @Size(max = 500, message = "Option C must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String optionC;

    @NotBlank(message = "Option D is required")
    @Size(max = 500, message = "Option D must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String optionD;

    @NotBlank(message = "Correct answer is required")
    @Pattern(regexp = "^[A-D]$", message = "Correct answer must be A, B, C, or D")
    @Column(nullable = false, length = 1)
    private String correctAnswer;

    @Size(max = 1000, message = "Explanation must not exceed 1000 characters")
    @Column(length = 1000)
    private String explanation;

    @NotNull(message = "Question bank is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_bank_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_question_question_bank"))
    private QuestionBank questionBank;

    @Column(nullable = false)
    private boolean active = true;

    @Min(value = 1, message = "Marks must be at least 1")
    @Max(value = 10, message = "Marks cannot exceed 10")
    @Column(nullable = false)
    private int marks = 1;

    @Pattern(regexp = "^(EASY|MEDIUM|HARD)$", message = "Difficulty level must be EASY, MEDIUM, or HARD")
    @Column(length = 10)
    private String difficultyLevel = "MEDIUM";

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (difficultyLevel == null) {
            difficultyLevel = "MEDIUM";
        }
    }

    // Helper method to check if answer is correct
    public boolean isCorrectAnswer(String answer) {
        return correctAnswer != null && correctAnswer.equalsIgnoreCase(answer);
    }
}