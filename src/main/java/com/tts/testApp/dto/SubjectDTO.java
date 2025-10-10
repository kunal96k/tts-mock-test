
package com.tts.testApp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectDTO {

    private Long id;

    @NotBlank(message = "Subject name is required")
    @Size(min = 3, max = 100, message = "Subject name must be between 3 and 100 characters")
    private String name;

    @NotBlank(message = "Subject code is required")
    @Size(min = 3, max = 20, message = "Subject code must be between 3 and 20 characters")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Subject code must contain only uppercase letters, numbers, and hyphens (e.g., JAVA-101)")
    private String subjectCode;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private boolean active;
    private int totalQuestions;
    private int studentsEnrolled;
}
