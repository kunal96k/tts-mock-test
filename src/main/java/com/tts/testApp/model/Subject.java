package com.tts.testApp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "subjects", indexes = {
        @Index(name = "idx_subject_code", columnList = "subjectCode"),
        @Index(name = "idx_subject_name", columnList = "name")
})
public class Subject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Subject name is required")
    @Size(min = 3, max = 100, message = "Subject name must be between 3 and 100 characters")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank(message = "Subject code is required")
    @Size(min = 3, max = 20, message = "Subject code must be between 3 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Subject code must contain only uppercase letters, numbers, and hyphens")
    @Column(nullable = false, unique = true, length = 20)
    private String subjectCode;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private int totalQuestions = 0;

    @Column(nullable = false)
    private int studentsEnrolled = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}