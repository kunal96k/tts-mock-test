package com.tts.testApp.repository;

import com.tts.testApp.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    /**
     * Find all active questions by question bank ID
     */
    List<Question> findByQuestionBankIdAndActiveTrue(Long questionBankId);

    /**
     * Find questions by difficulty level
     */
    List<Question> findByQuestionBankIdAndDifficultyLevelAndActiveTrue(
            Long questionBankId, String difficultyLevel);

    /**
     * Count active questions in a question bank
     */
    long countByQuestionBankIdAndActiveTrue(Long questionBankId);

    /**
     * Find random questions using native query (alternative approach)
     */
    @Query(value = "SELECT * FROM questions WHERE question_bank_id = :questionBankId " +
            "AND active = true ORDER BY RAND() LIMIT :limit",
            nativeQuery = true)
    List<Question> findRandomQuestions(
            @Param("questionBankId") Long questionBankId,
            @Param("limit") int limit);

    /**
     * Count questions by difficulty
     */
    long countByQuestionBankIdAndDifficultyLevelAndActiveTrue(
            Long questionBankId, String difficultyLevel);

    /**
     * Find all questions by question bank (including inactive)
     */
    List<Question> findByQuestionBankId(Long questionBankId);

    /**
     * Check if question exists in question bank
     */
    boolean existsByIdAndQuestionBankId(Long id, Long questionBankId);

    void deleteByQuestionBankId(Long questionBankId);

}