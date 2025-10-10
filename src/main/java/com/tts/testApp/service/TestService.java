package com.tts.testApp.service;

import com.tts.testApp.dto.TestConfigDTO;
import com.tts.testApp.dto.TestResultDTO;
import com.tts.testApp.dto.TestSubmissionDTO;
import com.tts.testApp.model.TestAttempt;
import com.tts.testApp.repository.QuestionRepository;
import com.tts.testApp.repository.TestAttemptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestService {

    private final TestAttemptRepository testAttemptRepository;
    private final QuestionRepository questionRepository;

    /**
     * Save test attempt to database
     */
    @Transactional
    public void saveTestAttempt(TestSubmissionDTO submission, TestResultDTO result, String username) {
        try {
            TestAttempt attempt = new TestAttempt();
            attempt.setStudentId(submission.getStudentId());
            attempt.setTestId(submission.getTestId());
            attempt.setQuestionBankId(submission.getQuestionBankId());
            attempt.setTotalQuestions(submission.getAnswers().size());
            attempt.setCorrectAnswers(result.getCorrectAnswers());
            attempt.setWrongAnswers(result.getWrongAnswers());
            attempt.setUnanswered(result.getUnanswered());
            attempt.setTotalMarks(result.getTotalMarks());
            attempt.setObtainedMarks(result.getObtainedMarks());
            attempt.setScorePercentage(result.getScorePercentage());
            attempt.setGrade(result.getGrade());
            attempt.setPassed(result.isPassed());
            attempt.setTimeTakenSeconds(submission.getTimeTakenSeconds());
            attempt.setTabSwitches(submission.getTabSwitches());
            attempt.setAttemptDate(LocalDateTime.now());
            attempt.setUsername(username);

            testAttemptRepository.save(attempt);
            log.info("Test attempt saved successfully for user: {}", username);

        } catch (Exception e) {
            log.error("Failed to save test attempt", e);
            throw new RuntimeException("Failed to save test attempt", e);
        }
    }

    /**
     * Get question bank statistics
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getQuestionBankStats(Long questionBankId) {
        Map<String, Object> stats = new HashMap<>();

        long totalQuestions = questionRepository.countByQuestionBankIdAndActiveTrue(questionBankId);
        long easyQuestions = questionRepository.countByQuestionBankIdAndDifficultyLevelAndActiveTrue(
                questionBankId, "EASY");
        long mediumQuestions = questionRepository.countByQuestionBankIdAndDifficultyLevelAndActiveTrue(
                questionBankId, "MEDIUM");
        long hardQuestions = questionRepository.countByQuestionBankIdAndDifficultyLevelAndActiveTrue(
                questionBankId, "HARD");

        stats.put("totalQuestions", totalQuestions);
        stats.put("easyQuestions", easyQuestions);
        stats.put("mediumQuestions", mediumQuestions);
        stats.put("hardQuestions", hardQuestions);
        stats.put("available", totalQuestions > 0);

        return stats;
    }

    /**
     * Validate test configuration
     */
    @Transactional(readOnly = true)
    public Map<String, Object> validateTestConfiguration(TestConfigDTO config) {
        Map<String, Object> validation = new HashMap<>();

        long availableQuestions = questionRepository.countByQuestionBankIdAndActiveTrue(
                config.getQuestionBankId());

        boolean isValid = true;
        StringBuilder errors = new StringBuilder();

        if (availableQuestions == 0) {
            isValid = false;
            errors.append("No questions available in this question bank. ");
        } else if (config.getTotalQuestions() > availableQuestions) {
            isValid = false;
            errors.append(String.format(
                    "Not enough questions. Required: %d, Available: %d. ",
                    config.getTotalQuestions(), availableQuestions));
        }

        // Check difficulty distribution if specified
        if (config.getEasyCount() != null && config.getMediumCount() != null
                && config.getHardCount() != null) {

            long easyAvailable = questionRepository.countByQuestionBankIdAndDifficultyLevelAndActiveTrue(
                    config.getQuestionBankId(), "EASY");
            long mediumAvailable = questionRepository.countByQuestionBankIdAndDifficultyLevelAndActiveTrue(
                    config.getQuestionBankId(), "MEDIUM");
            long hardAvailable = questionRepository.countByQuestionBankIdAndDifficultyLevelAndActiveTrue(
                    config.getQuestionBankId(), "HARD");

            if (config.getEasyCount() > easyAvailable) {
                isValid = false;
                errors.append(String.format(
                        "Not enough EASY questions. Required: %d, Available: %d. ",
                        config.getEasyCount(), easyAvailable));
            }
            if (config.getMediumCount() > mediumAvailable) {
                isValid = false;
                errors.append(String.format(
                        "Not enough MEDIUM questions. Required: %d, Available: %d. ",
                        config.getMediumCount(), mediumAvailable));
            }
            if (config.getHardCount() > hardAvailable) {
                isValid = false;
                errors.append(String.format(
                        "Not enough HARD questions. Required: %d, Available: %d. ",
                        config.getHardCount(), hardAvailable));
            }
        }

        validation.put("valid", isValid);
        validation.put("availableQuestions", availableQuestions);
        validation.put("errors", errors.toString().trim());

        return validation;
    }
}