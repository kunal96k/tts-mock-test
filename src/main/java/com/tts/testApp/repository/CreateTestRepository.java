package com.tts.testApp.repository;

import com.tts.testApp.model.CreateTest;
import com.tts.testApp.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreateTestRepository extends JpaRepository<CreateTest, Long> {

    /**
     * Find test by name
     */
    Optional<CreateTest> findByTestName(String testName);

    /**
     * Check if test exists by name
     */
    boolean existsByTestName(String testName);

    /**
     * Find all active tests
     */
    List<CreateTest> findByActiveTrue();

    /**
     * Find tests by type (MOCK or FINAL)
     */
    List<CreateTest> findByTestType(String testType);

    /**
     * Find tests by subject - ADDED THIS METHOD
     */
    List<CreateTest> findBySubject(Subject subject);

    /**
     * Find tests by subject ID - ALTERNATIVE METHOD
     */
    List<CreateTest> findBySubjectId(Long subjectId);

    /**
     * Find active tests by subject
     */
    List<CreateTest> findByActiveTrueAndSubject(Subject subject);

    /**
     * Find all tests ordered by creation date (newest first)
     */
    @Query("SELECT t FROM CreateTest t ORDER BY t.createdAt DESC")
    List<CreateTest> findAllOrderByCreatedAtDesc();

    /**
     * Count active tests
     */
    long countByActiveTrue();

    /**
     * Count tests by subject
     */
    long countBySubject(Subject subject);

    /**
     * Find active tests by type
     */
    List<CreateTest> findByActiveTrueAndTestType(String testType);
}