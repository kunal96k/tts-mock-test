package com.tts.testApp.repository;

import com.tts.testApp.model.QuestionBank;
import com.tts.testApp.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionBankRepository extends JpaRepository<QuestionBank, Long> {

    List<QuestionBank> findBySubject(Subject subject);

    List<QuestionBank> findBySubjectId(Long subjectId);

    List<QuestionBank> findByActiveTrue();

    List<QuestionBank> findBySubjectIdAndActiveTrue(Long subjectId);

    Optional<QuestionBank> findByFileName(String fileName);

    boolean existsByFileName(String fileName);

    @Query("SELECT qb FROM QuestionBank qb WHERE qb.subject.id = :subjectId ORDER BY qb.lastUpdated DESC")
    List<QuestionBank> findBySubjectIdOrderByLastUpdatedDesc(@Param("subjectId") Long subjectId);

    @Query("SELECT COUNT(qb) FROM QuestionBank qb WHERE qb.subject.id = :subjectId AND qb.active = true")
    long countActiveBySubjectId(@Param("subjectId") Long subjectId);

    @Query("SELECT SUM(qb.totalQuestions) FROM QuestionBank qb WHERE qb.subject.id = :subjectId AND qb.active = true")
    Integer sumTotalQuestionsBySubjectId(@Param("subjectId") Long subjectId);

}