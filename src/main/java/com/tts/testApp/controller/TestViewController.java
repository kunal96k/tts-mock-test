package com.tts.testApp.controller;

import com.tts.testApp.service.QuestionService;
import com.tts.testApp.service.TestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestViewController {

    private final TestService testService;
    private final QuestionService questionService;

    /**
     * Show test start page with comprehensive logging
     */
    @GetMapping("/start/{testId}")
    @PreAuthorize("hasRole('STUDENT')")
    public String showTestStartPage(@PathVariable Long testId, Model model, Principal principal) {
        log.info("========== TEST VIEW CONTROLLER START ==========");
        log.info("User: {}, TestId: {}", principal.getName(), testId);

        try {
            // TODO: Replace hardcoded values with actual test lookup
            Long questionBankId = 2L;
            String testName = "Python Programming Final Test";
            Integer totalQuestions = 30;
            Integer duration = 30;
            Integer passingPercentage = 35;
            Integer marksPerQuestion = 1;
            Integer tabSwitchLimit = 3;

            log.info("Test Configuration:");
            log.info("  - QuestionBankId: {}", questionBankId);
            log.info("  - TestName: {}", testName);
            log.info("  - TotalQuestions: {}", totalQuestions);
            log.info("  - Duration: {} minutes", duration);
            log.info("  - PassingPercentage: {}%", passingPercentage);
            log.info("  - TabSwitchLimit: {}", tabSwitchLimit);

            // Get question bank statistics
            log.info("Fetching question bank statistics...");
            Map<String, Object> stats = testService.getQuestionBankStats(questionBankId);
            log.info("Question Bank Stats: {}", stats);

            // Validate that questions are available
            Long totalAvailable = (Long) stats.get("totalQuestions");
            if (totalAvailable == null || totalAvailable == 0) {
                log.error("ERROR: No questions available in question bank {}", questionBankId);
                model.addAttribute("error", "No questions available in this question bank");
                return "error";
            }

            if (totalQuestions > totalAvailable) {
                log.warn("WARNING: Requested {} questions but only {} available",
                        totalQuestions, totalAvailable);
                totalQuestions = totalAvailable.intValue();
            }

            // Add all attributes to model
            model.addAttribute("testId", testId);
            model.addAttribute("questionBankId", questionBankId);
            model.addAttribute("testName", testName);
            model.addAttribute("totalQuestions", totalQuestions);
            model.addAttribute("duration", duration);
            model.addAttribute("passingPercentage", passingPercentage);
            model.addAttribute("marksPerQuestion", marksPerQuestion);
            model.addAttribute("tabSwitchLimit", tabSwitchLimit);
            model.addAttribute("studentId", 1L); // TODO: Get from authenticated user
            model.addAttribute("questionBankStats", stats);

            log.info("Model attributes added successfully");
            log.info("========== TEST VIEW CONTROLLER END ==========");

            return "start-test";

        } catch (Exception e) {
            log.error("========== ERROR IN TEST VIEW CONTROLLER ==========");
            log.error("Error loading test page", e);
            log.error("Exception type: {}", e.getClass().getName());
            log.error("Exception message: {}", e.getMessage());
            if (e.getCause() != null) {
                log.error("Cause: {}", e.getCause().getMessage());
            }

            model.addAttribute("error", "Failed to load test: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Show student's test history
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('STUDENT')")
    public String showTestHistory(Model model, Principal principal) {
        try {
            log.info("Loading test history for user: {}", principal.getName());
            // model.addAttribute("attempts", testService.getStudentTestHistory(principal.getName()));
            return "test-history";
        } catch (Exception e) {
            log.error("Error loading test history", e);
            model.addAttribute("error", "Failed to load test history.");
            return "error";
        }
    }
}