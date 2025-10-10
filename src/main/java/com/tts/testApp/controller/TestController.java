package com.tts.testApp.controller;

import com.tts.testApp.dto.*;
import com.tts.testApp.service.QuestionService;
import com.tts.testApp.service.TestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final QuestionService questionService;
    private final TestService testService;

    /**
     * Initialize test - fetch random questions
     */
    @PostMapping("/initialize")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> initializeTest(@Valid @RequestBody TestConfigDTO config,
                                            Principal principal) {
        try {
            log.info("Initializing test for user: {}, questionBankId: {}",
                    principal.getName(), config.getQuestionBankId());

            List<QuestionDTO> questions = (config.getEasyCount() != null &&
                    config.getMediumCount() != null &&
                    config.getHardCount() != null)
                    ? questionService.getRandomQuestionsByDifficulty(
                    config.getQuestionBankId(),
                    config.getEasyCount(),
                    config.getMediumCount(),
                    config.getHardCount())
                    : questionService.getRandomQuestionsForTest(
                    config.getQuestionBankId(),
                    config.getTotalQuestions());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("questions", questions);
            response.put("totalQuestions", questions.size());
            response.put("duration", config.getDurationMinutes());
            response.put("passingPercentage", config.getPassingPercentage());
            response.put("tabSwitchLimit", config.getTabSwitchLimit());

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            log.error("Test initialization failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error during test initialization", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Failed to initialize test"));
        }
    }

    /**
     * Submit test and get results
     */
    @PostMapping("/submit")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> submitTest(@Valid @RequestBody TestSubmissionDTO submission,
                                        Principal principal) {
        try {
            log.info("Test submission from user: {}, testId: {}",
                    principal.getName(), submission.getTestId());

            TestResultDTO result = questionService.validateAnswers(submission.getAnswers());
            result.setGrade(result.calculateGrade());
            result.setPassed(result.getScorePercentage() >= 35);

            testService.saveTestAttempt(submission, result, principal.getName());

            return ResponseEntity.ok(Map.of("success", true, "result", result));

        } catch (Exception e) {
            log.error("Test submission failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Failed to submit test"));
        }
    }

    /**
     * Get question bank statistics
     */
    @GetMapping("/question-bank/{id}/stats")
    @PreAuthorize("hasAnyRole('STUDENT', 'ADMIN')")
    public ResponseEntity<?> getQuestionBankStats(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(testService.getQuestionBankStats(id));
        } catch (Exception e) {
            log.error("Failed to fetch question bank stats", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", "Failed to fetch statistics"));
        }
    }

    /**
     * Validate test configuration
     */
    @PostMapping("/validate-config")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> validateTestConfig(@Valid @RequestBody TestConfigDTO config) {
        try {
            return ResponseEntity.ok(testService.validateTestConfiguration(config));
        } catch (Exception e) {
            log.error("Config validation failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}
