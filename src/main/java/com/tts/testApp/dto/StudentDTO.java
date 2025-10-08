package com.tts.testApp.dto;

import com.tts.testApp.model.Student;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentDTO {

    private Long id;

    private String studentId;

    @NotBlank(message = "Full name is required")
    @Size(min = 3, max = 100, message = "Full name must be between 3 and 100 characters")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Full name can only contain letters and spaces")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, and one number")
    private String password;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String confirmPassword;

    private boolean enabled = true;

    private LocalDateTime registeredDate;

    private int testsTaken;

    // Constructor from Entity - used for responses
    public StudentDTO(Student student) {
        this.id = student.getId();
        this.studentId = student.getStudentId();
        this.fullName = student.getFullName();
        this.email = student.getEmail();
        this.enabled = student.isEnabled();
        this.registeredDate = student.getRegisteredDate();
        this.testsTaken = student.getTestsTaken();
    }
}