package com.tts.testApp.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionBankDTO {

    private Long id;

    @NotNull(message = "Subject is required")
    private Long subjectId;

    private String subjectName;

    @NotNull(message = "Please select a CSV file to upload")
    private MultipartFile file;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    private String fileName;
    private String originalFileName;
    private int totalQuestions;
    private boolean active;
    private Long fileSize;
    private LocalDateTime lastUpdated;
    private String uploadedBy;

    // Validation for file
    public boolean isValidFile() {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String filename = file.getOriginalFilename();
        if (filename == null) {
            return false;
        }

        // Check file extension
        return filename.toLowerCase().endsWith(".csv");
    }

    public boolean isFileSizeValid() {
        if (file == null) {
            return false;
        }
        // Max 5MB
        return file.getSize() <= 5 * 1024 * 1024;
    }

    public String getFileExtension() {
        if (file == null || file.getOriginalFilename() == null) {
            return "";
        }
        String filename = file.getOriginalFilename();
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }
}