package com.tts.testApp.config;

import com.tts.testApp.dto.SubjectDTO;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.tts.testApp.controller")
public class GlobalControllerAdvice {

    /**
     * Makes SubjectDTO available to all controllers
     * This ensures the form always has a backing object
     */
    @ModelAttribute("subjectDTO")
    public SubjectDTO subjectDTO() {
        SubjectDTO dto = new SubjectDTO();
        dto.setActive(true); // Default to active
        return dto;
    }
}