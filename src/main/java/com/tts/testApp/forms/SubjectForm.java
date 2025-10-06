package com.tts.testApp.forms;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubjectForm {

    @NotBlank(message = "Subject name is required")
    @Size(min = 2, max = 100, message = "Subject name must be between 2 and 100 characters")
    private String name;

    @NotBlank(message = "Subject code is required")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Subject code must contain only uppercase letters, numbers, and hyphens")
    @Size(min = 2, max = 20, message = "Subject code must be between 2 and 20 characters")
    private String code;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

}
