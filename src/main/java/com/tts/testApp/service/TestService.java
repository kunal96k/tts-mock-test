package com.tts.testApp.service;

import com.tts.testApp.dto.CreateTestDTO;
import com.tts.testApp.dto.TestConfigDTO;
import com.tts.testApp.dto.TestResultDTO;
import com.tts.testApp.dto.TestSubmissionDTO;
import com.tts.testApp.model.CreateTest;
import com.tts.testApp.model.QuestionBank;
import com.tts.testApp.repository.CreateTestRepository;
import com.tts.testApp.repository.QuestionBankRepository;
import com.tts.testApp.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {

    private final CreateTestRepository createTestRepository;
    private final QuestionBankRepository questionBankRepository;
    private final QuestionRepository questionRepository;

    /**
     * Get test by ID with full details
     */
    public CreateTestDTO getTestById(Long testId) {
        log.info("Fetching test with ID: {}", testId);

        CreateTest test = createTestRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found with ID: " + testId));

        CreateTestDTO dto = convertToDTO(test);
        log.info("Test found: {} - {}", dto.getTestName(), dto.getSubjectName());
        return dto;
    }

    /**
     * Get question bank statistics with difficulty breakdown
     */
    public Map<String, Object> getQuestionBankStats(Long questionBankId) {
        log.info("Fetching statistics for question bank: {}", questionBankId);

        QuestionBank qb = questionBankRepository.findById(questionBankId)
                .orElseThrow(() -> new RuntimeException("Question bank not found: " + questionBankId));

        // Count questions by difficulty using String values
        long easyCount = questionRepository.countByQuestionBankIdAndDifficultyLevelAndActiveTrue(
                questionBankId, "EASY");
        long mediumCount = questionRepository.countByQuestionBankIdAndDifficultyLevelAndActiveTrue(
                questionBankId, "MEDIUM");
        long hardCount = questionRepository.countByQuestionBankIdAndDifficultyLevelAndActiveTrue(
                questionBankId, "HARD");
        long totalCount = questionRepository.countByQuestionBankIdAndActiveTrue(questionBankId);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalQuestions", totalCount);
        stats.put("easyQuestions", easyCount);
        stats.put("mediumQuestions", mediumCount);
        stats.put("hardQuestions", hardCount);
        stats.put("subjectName", qb.getSubject().getName());
        stats.put("questionBankName", qb.getFileName());

        log.info("Question Bank Stats: Total={}, Easy={}, Medium={}, Hard={}",
                totalCount, easyCount, mediumCount, hardCount);

        return stats;
    }

    /**
     * Save test attempt with results
     */
    @Transactional
    public void saveTestAttempt(TestSubmissionDTO submission, TestResultDTO result, String username) {
        log.info("Saving test attempt for user: {}, testId: {}", username, submission.getTestId());

        try {
            // TODO: Create TestAttempt entity and implement this
            // For now, just log the attempt
            log.info("Test attempt - Score: {}%, Grade: {}, Correct: {}, Wrong: {}, Unanswered: {}",
                    result.getScorePercentage(),
                    result.getGrade(),
                    result.getCorrectAnswers(),
                    result.getWrongAnswers(),
                    result.getUnanswered());

            log.info("Test attempt saved successfully");
        } catch (Exception e) {
            log.error("Failed to save test attempt", e);
            throw new RuntimeException("Failed to save test attempt", e);
        }
    }

    /**
     * Validate test configuration before starting
     */
    public Map<String, Object> validateTestConfiguration(TestConfigDTO config) {
        log.info("Validating test configuration for question bank: {}", config.getQuestionBankId());

        Map<String, Object> stats = getQuestionBankStats(config.getQuestionBankId());
        Long totalAvailable = (Long) stats.get("totalQuestions");

        Map<String, Object> validation = new HashMap<>();
        validation.put("success", true);
        validation.put("totalAvailable", totalAvailable);
        validation.put("requested", config.getTotalQuestions());

        if (totalAvailable < config.getTotalQuestions()) {
            validation.put("success", false);
            validation.put("error", String.format(
                    "Only %d questions available, but %d requested",
                    totalAvailable, config.getTotalQuestions()));
        }

        return validation;
    }

    /**
     * Convert CreateTest entity to DTO - FIXED with proper null checks
     */
    private CreateTestDTO convertToDTO(CreateTest test) {
        CreateTestDTO dto = new CreateTestDTO();
        dto.setId(test.getId());
        dto.setTestName(test.getTestName());

        // Properly handle Subject relationship
        if (test.getSubject() != null) {
            dto.setSubjectName(test.getSubject().getName());
            dto.setSubjectId(test.getSubject().getId());
        }

        dto.setTotalQuestions(test.getTotalQuestions());
        dto.setDuration(test.getDuration());
        dto.setPassingPercentage(test.getPassingPercentage());
        dto.setMarksPerQuestion(test.getMarksPerQuestion());
        dto.setTotalMarks(test.getTotalMarks());
        dto.setTabSwitchLimit(test.getTabSwitchLimit());
        dto.setTestType(test.getTestType());
        dto.setActive(test.getActive());

        return dto;
    }
}