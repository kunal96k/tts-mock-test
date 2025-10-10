package com.tts.testApp.controller;

import com.tts.testApp.dto.CreateTestDTO;
import com.tts.testApp.model.Subject;
import com.tts.testApp.repository.SubjectRepository;
import com.tts.testApp.service.CreateTestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;


@Controller
@RequestMapping("/admin/tests")
@RequiredArgsConstructor
@Slf4j
public class CreateTestController {

    private final CreateTestService createTestService;
    private final SubjectRepository subjectRepository;

    /**
     * Handle test creation with validation
     */
    @PostMapping("/create")
    public String createTest(
            @Valid @ModelAttribute("testDTO") CreateTestDTO testDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Processing test creation request for subject: {}", testDTO.getSubjectName());
        log.info("Test DTO received - Subject: {}, Type: {}, Questions: {}",
                testDTO.getSubjectName(), testDTO.getTestType(), testDTO.getTotalQuestions());

        // Validation errors
        if (bindingResult.hasErrors()) {
            log.warn("Validation errors found: {}", bindingResult.getAllErrors());
            bindingResult.getAllErrors().forEach(error ->
                    log.error("Validation error: {}", error.getDefaultMessage())
            );
            redirectAttributes.addFlashAttribute("testError",
                    "Please correct the errors: " + bindingResult.getAllErrors().get(0).getDefaultMessage());
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.testDTO", bindingResult);
            redirectAttributes.addFlashAttribute("testDTO", testDTO);
            return "redirect:/admin/dashboard";
        }

        try {
            // Validate that the subject exists in DB
            Subject subject = subjectRepository.findByName(testDTO.getSubjectName())
                    .orElseThrow(() -> new RuntimeException("Subject not found: " + testDTO.getSubjectName()));

            log.info("Subject found: ID={}, Name={}", subject.getId(), subject.getName());

            // Set the subject ID in DTO for proper mapping
            testDTO.setSubjectId(subject.getId());

            // The testName will be auto-generated in the service/entity
            // Calculate total marks
            testDTO.calculateTotalMarks();

            log.info("Calling service to create test with total marks: {}", testDTO.getTotalMarks());

            // Save the test
            CreateTestDTO savedTest = createTestService.createTest(testDTO);

            log.info("Test created successfully - ID: {}, Name: {}, Subject: {}",
                    savedTest.getId(), savedTest.getTestName(), savedTest.getSubjectName());

            // Success message
            redirectAttributes.addFlashAttribute("success",
                    "Test '" + savedTest.getTestName() + "' created successfully!");

            return "redirect:/admin/dashboard";

        } catch (Exception e) {
            log.error("Error creating test: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("testError",
                    "Failed to create test: " + e.getMessage());
            redirectAttributes.addFlashAttribute("testDTO", testDTO);
            return "redirect:/admin/dashboard";
        }
    }

    /**
     * Delete test
     */
    @PostMapping("/{id}/delete")
    public String deleteTest(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        log.info("Deleting test with ID: {}", id);

        try {
            createTestService.deleteTest(id);
            redirectAttributes.addFlashAttribute("success", "Test deleted successfully!");
        } catch (Exception e) {
            log.error("Error deleting test: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error",
                    "Failed to delete test: " + e.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    /**
     * Get test by ID (optional - for future use)
     */
    @GetMapping("/{id}")
    public String getTestById(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        log.info("Fetching test with ID: {}", id);

        try {
            CreateTestDTO test = createTestService.getTestById(id);
            model.addAttribute("test", test);
            return "admin/test-details";
        } catch (Exception e) {
            log.error("Error fetching test: {}", e.getMessage(), e);
            redirectAttributes.addFlashAttribute("error", "Test not found with ID: " + id);
            return "redirect:/admin/dashboard";
        }
    }
}