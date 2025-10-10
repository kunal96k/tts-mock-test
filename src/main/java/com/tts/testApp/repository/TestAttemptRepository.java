package com.tts.testApp.repository;

import com.tts.testApp.model.TestAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TestAttemptRepository extends JpaRepository<TestAttempt, Long> {

    /**
     * Find all attempts by student ID
     */
    List<TestAttempt> findByStudentIdOrderByAttemptDateDesc(Long studentId);

    /**
     * Find all attempts by test ID
     */
    List<TestAttempt> findByTestIdOrderByAttemptDateDesc(Long testId);

    /**
     * Find student's attempts for a specific test
     */
    List<TestAttempt> findByStudentIdAndTestIdOrderByAttemptDateDesc(Long studentId, Long testId);

    /**
     * Count total attempts by student
     */
    long countByStudentId(Long studentId);

    /**
     * Count passed attempts by student
     */
    long countByStudentIdAndPassedTrue(Long studentId);

    /**
     * Get student's best score for a test
     */
    @Query("SELECT MAX(ta.scorePercentage) FROM TestAttempt ta " +
            "WHERE ta.studentId = :studentId AND ta.testId = :testId")
    Double findBestScoreByStudentAndTest(@Param("studentId") Long studentId,
                                         @Param("testId") Long testId);

    /**
     * Get student's latest attempt for a test
     */
    TestAttempt findFirstByStudentIdAndTestIdOrderByAttemptDateDesc(Long studentId, Long testId);

    /**
     * Find attempts within date range
     */
    List<TestAttempt> findByAttemptDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get average score for a test
     */
    @Query("SELECT AVG(ta.scorePercentage) FROM TestAttempt ta WHERE ta.testId = :testId")
    Double findAverageScoreByTest(@Param("testId") Long testId);

    /**
     * Count attempts for a test
     */
    long countByTestId(Long testId);

    /**
     * Find top performers for a test
     */
    @Query("SELECT ta FROM TestAttempt ta WHERE ta.testId = :testId " +
            "ORDER BY ta.scorePercentage DESC, ta.timeTakenSeconds ASC")
    List<TestAttempt> findTopPerformersByTest(@Param("testId") Long testId);
}