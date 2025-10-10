package com.tts.testApp.service;

import com.tts.testApp.dto.AnswerReviewDTO;
import com.tts.testApp.dto.QuestionDTO;
import com.tts.testApp.dto.StudentAnswerDTO;
import com.tts.testApp.dto.TestResultDTO;
import com.tts.testApp.model.Question;
import com.tts.testApp.model.QuestionBank;
import com.tts.testApp.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionService {

    private final QuestionRepository questionRepository;

    /**
     * Fetch random questions for a test
     * @param questionBankId The question bank ID
     * @param requiredCount Number of questions needed (e.g., 30)
     * @return List of shuffled question DTOs
     */
    @Transactional(readOnly = true)
    public List<QuestionDTO> getRandomQuestionsForTest(Long questionBankId, int requiredCount) {
        log.info("Fetching {} random questions from question bank {}", requiredCount, questionBankId);

        // Fetch all active questions from the question bank
        List<Question> allQuestions = questionRepository
                .findByQuestionBankIdAndActiveTrue(questionBankId);

        if (allQuestions.isEmpty()) {
            throw new IllegalStateException("No questions found in question bank: " + questionBankId);
        }

        if (allQuestions.size() < requiredCount) {
            log.warn("Question bank has only {} questions, but {} required",
                    allQuestions.size(), requiredCount);
            throw new IllegalStateException(
                    String.format("Not enough questions available. Required: %d, Available: %d",
                            requiredCount, allQuestions.size())
            );
        }

        // Shuffle all questions
        Collections.shuffle(allQuestions);

        // Take only the required number
        List<Question> selectedQuestions = allQuestions.subList(0, requiredCount);

        // Convert to DTOs (without revealing correct answers)
        List<QuestionDTO> questionDTOs = selectedQuestions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        log.info("Successfully prepared {} random questions", questionDTOs.size());
        return questionDTOs;
    }

    /**
     * Get random questions with difficulty distribution
     * @param questionBankId The question bank ID
     * @param easyCount Number of easy questions
     * @param mediumCount Number of medium questions
     * @param hardCount Number of hard questions
     * @return List of shuffled questions
     */
    @Transactional(readOnly = true)
    public List<QuestionDTO> getRandomQuestionsByDifficulty(
            Long questionBankId, int easyCount, int mediumCount, int hardCount) {

        log.info("Fetching questions by difficulty: Easy={}, Medium={}, Hard={}",
                easyCount, mediumCount, hardCount);

        List<QuestionDTO> selectedQuestions = new ArrayList<>();

        // Fetch and shuffle EASY questions
        if (easyCount > 0) {
            List<Question> easyQuestions = questionRepository
                    .findByQuestionBankIdAndDifficultyLevelAndActiveTrue(questionBankId, "EASY");
            Collections.shuffle(easyQuestions);
            selectedQuestions.addAll(easyQuestions.stream()
                    .limit(easyCount)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList()));
        }

        // Fetch and shuffle MEDIUM questions
        if (mediumCount > 0) {
            List<Question> mediumQuestions = questionRepository
                    .findByQuestionBankIdAndDifficultyLevelAndActiveTrue(questionBankId, "MEDIUM");
            Collections.shuffle(mediumQuestions);
            selectedQuestions.addAll(mediumQuestions.stream()
                    .limit(mediumCount)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList()));
        }

        // Fetch and shuffle HARD questions
        if (hardCount > 0) {
            List<Question> hardQuestions = questionRepository
                    .findByQuestionBankIdAndDifficultyLevelAndActiveTrue(questionBankId, "HARD");
            Collections.shuffle(hardQuestions);
            selectedQuestions.addAll(hardQuestions.stream()
                    .limit(hardCount)
                    .map(this::convertToDTO)
                    .collect(Collectors.toList()));
        }

        // Shuffle the final list to mix difficulty levels
        Collections.shuffle(selectedQuestions);

        log.info("Successfully prepared {} questions with mixed difficulty", selectedQuestions.size());
        return selectedQuestions;
    }

    /**
     * Convert Question entity to DTO (without correct answer for security)
     */
    private QuestionDTO convertToDTO(Question question) {
        QuestionDTO dto = new QuestionDTO();
        dto.setId(question.getId());
        dto.setQuestionText(question.getQuestionText());
        dto.setOptionA(question.getOptionA());
        dto.setOptionB(question.getOptionB());
        dto.setOptionC(question.getOptionC());
        dto.setOptionD(question.getOptionD());
        dto.setMarks(question.getMarks());
        dto.setDifficultyLevel(question.getDifficultyLevel());
        // DO NOT include correctAnswer - it will be validated on server side
        return dto;
    }

    /**
     * Validate student answers and calculate score
     */
    @Transactional(readOnly = true)
    public TestResultDTO validateAnswers(List<StudentAnswerDTO> studentAnswers) {
        TestResultDTO result = new TestResultDTO();
        int correctCount = 0;
        int totalMarks = 0;
        int obtainedMarks = 0;

        List<AnswerReviewDTO> reviewData = new ArrayList<>();

        for (StudentAnswerDTO answer : studentAnswers) {
            Question question = questionRepository.findById(answer.getQuestionId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Question not found: " + answer.getQuestionId()));

            totalMarks += question.getMarks();

            AnswerReviewDTO review = new AnswerReviewDTO();
            review.setQuestionId(question.getId());
            review.setQuestionText(question.getQuestionText());
            review.setUserAnswer(answer.getSelectedOption());
            review.setCorrectAnswer(question.getCorrectAnswer());

            if (answer.getSelectedOption() != null &&
                    answer.getSelectedOption().equalsIgnoreCase(question.getCorrectAnswer())) {
                correctCount++;
                obtainedMarks += question.getMarks();
                review.setStatus("correct");
            } else if (answer.getSelectedOption() == null) {
                review.setStatus("unanswered");
            } else {
                review.setStatus("incorrect");
            }

            review.setExplanation(question.getExplanation());
            reviewData.add(review);
        }

        result.setCorrectAnswers(correctCount);
        result.setWrongAnswers(studentAnswers.size() - correctCount -
                (int) studentAnswers.stream().filter(a -> a.getSelectedOption() == null).count());
        result.setUnanswered((int) studentAnswers.stream()
                .filter(a -> a.getSelectedOption() == null).count());
        result.setTotalMarks(totalMarks);
        result.setObtainedMarks(obtainedMarks);
        result.setScorePercentage((double) obtainedMarks / totalMarks * 100);
        result.setReviewData(reviewData);

        return result;
    }
}