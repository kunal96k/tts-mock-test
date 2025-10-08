package com.tts.testApp.repository;

import com.tts.testApp.model.Question;
import com.tts.testApp.model.QuestionBank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {

    List<Question> findByQuestionBank(QuestionBank questionBank);

    List<Question> findByQuestionBankId(Long questionBankId);

    List<Question> findByQuestionBankIdAndActiveTrue(Long questionBankId);

    List<Question> findByDifficultyLevel(String difficultyLevel);

    @Query("SELECT q FROM Question q WHERE q.questionBank.subject.id = :subjectId AND q.active = true")
    List<Question> findActiveQuestionsBySubjectId(@Param("subjectId") Long subjectId);

    @Query("SELECT q FROM Question q WHERE q.questionBank.subject.id = :subjectId AND q.active = true ORDER BY RAND()")
    List<Question> findRandomQuestionsBySubjectId(@Param("subjectId") Long subjectId);

    @Query("SELECT COUNT(q) FROM Question q WHERE q.questionBank.id = :questionBankId AND q.active = true")
    long countActiveByQuestionBankId(@Param("questionBankId") Long questionBankId);

    void deleteByQuestionBankId(Long questionBankId);
}