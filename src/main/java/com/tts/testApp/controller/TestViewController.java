package com.tts.testApp.controller;

import com.tts.testApp.dto.CreateTestDTO;
import com.tts.testApp.model.CreateTest;
import com.tts.testApp.model.QuestionBank;
import com.tts.testApp.model.Subject;
import com.tts.testApp.repository.CreateTestRepository;
import com.tts.testApp.repository.QuestionBankRepository;
import com.tts.testApp.repository.QuestionRepository;
import com.tts.testApp.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/test")
@RequiredArgsConstructor
@Slf4j
public class TestViewController {

    private final CreateTestRepository createTestRepository;
    private final QuestionBankRepository questionBankRepository;
    private final QuestionRepository questionRepository;
    private final StudentService studentService;

    /**
     * Show test start page with comprehensive logging
     */
    @GetMapping("/start/{testId}")
    @PreAuthorize("hasRole('STUDENT')")
    public String showTestStartPage(@PathVariable Long testId, Model model, Principal principal) {
        log.info("========== TEST VIEW CONTROLLER START ==========");
        log.info("User: {}, TestId: {}", principal.getName(), testId);

        try {
            // Fetch test configuration dynamically from CreateTest entity
            CreateTest test = createTestRepository.findById(testId)
                    .orElseThrow(() -> new RuntimeException("Test not found with ID: " + testId));

            log.info("Test Configuration Loaded:");
            log.info("  - TestName: {}", test.getTestName());
            log.info("  - Subject: {}", test.getSubject().getName());
            log.info("  - TotalQuestions: {}", test.getTotalQuestions());
            log.info("  - Duration: {} minutes", test.getDuration());
            log.info("  - PassingPercentage: {}%", test.getPassingPercentage());
            log.info("  - MarksPerQuestion: {}", test.getMarksPerQuestion());
            log.info("  - TotalMarks: {}", test.getTotalMarks());
            log.info("  - TabSwitchLimit: {}", test.getTabSwitchLimit());

            // Get the question bank associated with this subject
            Subject subject = test.getSubject();
            List<QuestionBank> questionBanks = questionBankRepository
                    .findBySubjectIdAndActiveTrue(subject.getId());

            if (questionBanks.isEmpty()) {
                log.error("ERROR: No active question banks for subject: {}", subject.getName());
                model.addAttribute("error", "No question banks available for this subject");
                return "error";
            }

            // Use the first active question bank (you can enhance this logic)
            QuestionBank questionBank = questionBanks.get(0);
            log.info("Using Question Bank: {} (ID: {})", questionBank.getFileName(), questionBank.getId());

            // Get question bank statistics
            long totalAvailable = questionRepository.countByQuestionBankIdAndActiveTrue(questionBank.getId());
            long easyCount = questionRepository.countByQuestionBankIdAndDifficultyLevelAndActiveTrue(
                    questionBank.getId(), "EASY");
            long mediumCount = questionRepository.countByQuestionBankIdAndDifficultyLevelAndActiveTrue(
                    questionBank.getId(), "MEDIUM");
            long hardCount = questionRepository.countByQuestionBankIdAndDifficultyLevelAndActiveTrue(
                    questionBank.getId(), "HARD");

            log.info("Question Bank Stats: Total={}, Easy={}, Medium={}, Hard={}",
                    totalAvailable, easyCount, mediumCount, hardCount);

            // Validate question availability
            if (totalAvailable == 0) {
                log.error("ERROR: No questions available in question bank {}", questionBank.getId());
                model.addAttribute("error", "No questions available for this test");
                return "error";
            }

            if (test.getTotalQuestions() > totalAvailable) {
                log.warn("WARNING: Requested {} questions but only {} available",
                        test.getTotalQuestions(), totalAvailable);
            }

            // Get authenticated student ID
            Long studentId = studentService.findByEmail(principal.getName())
                    .map(student -> student.getId())
                    .orElseThrow(() -> new RuntimeException("Student not found"));

            // Prepare question bank stats for display
            Map<String, Object> questionBankStats = new HashMap<>();
            questionBankStats.put("totalQuestions", totalAvailable);
            questionBankStats.put("easyQuestions", easyCount);
            questionBankStats.put("mediumQuestions", mediumCount);
            questionBankStats.put("hardQuestions", hardCount);

            // Add all attributes to model
            model.addAttribute("testId", testId);
            model.addAttribute("questionBankId", questionBank.getId());
            model.addAttribute("testName", test.getTestName());
            model.addAttribute("subjectName", subject.getName());
            model.addAttribute("totalQuestions", test.getTotalQuestions());
            model.addAttribute("duration", test.getDuration());
            model.addAttribute("passingPercentage", test.getPassingPercentage());
            model.addAttribute("marksPerQuestion", test.getMarksPerQuestion());
            model.addAttribute("totalMarks", test.getTotalMarks());
            model.addAttribute("tabSwitchLimit", test.getTabSwitchLimit());
            model.addAttribute("studentId", studentId);
            model.addAttribute("questionBankStats", questionBankStats);

            log.info("Model attributes added successfully");
            log.info("========== TEST VIEW CONTROLLER END ==========");

            return "start-test";

        } catch (Exception e) {
            log.error("========== ERROR IN TEST VIEW CONTROLLER ==========");
            log.error("Error loading test page", e);
            log.error("Exception: {} - {}", e.getClass().getName(), e.getMessage());

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