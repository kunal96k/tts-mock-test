package com.tts.testApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "question_banks", indexes = {
        @Index(name = "idx_subject_id", columnList = "subject_id"),
        @Index(name = "idx_file_name", columnList = "fileName")
})
public class QuestionBank {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "File name is required")
    @Size(max = 255, message = "File name must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String fileName;

    @NotBlank(message = "Original file name is required")
    @Size(max = 255, message = "Original file name must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String originalFileName;

    @NotBlank(message = "File path is required")
    @Column(nullable = false, length = 500)
    private String filePath;

    @NotNull(message = "Subject is required")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_question_bank_subject"))
    private Subject subject;

    @Min(value = 0, message = "Total questions cannot be negative")
    @Column(nullable = false)
    private int totalQuestions = 0;

    @Column(nullable = false)
    private boolean active = true;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @NotNull(message = "File size is required")
    @Min(value = 0, message = "File size cannot be negative")
    @Column(nullable = false)
    private Long fileSize; // in bytes

    @OneToMany(mappedBy = "questionBank", cascade = CascadeType.ALL,
            orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Question> questions = new ArrayList<>();

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @Column(length = 100)
    private String uploadedBy;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    // Helper methods
    public void addQuestion(Question question) {
        questions.add(question);
        question.setQuestionBank(this);
    }

    public void removeQuestion(Question question) {
        questions.remove(question);
        question.setQuestionBank(null);
    }
}