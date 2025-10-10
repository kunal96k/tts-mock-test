package com.tts.testApp.repository;

import com.tts.testApp.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Check if a student exists with the given email
     * @param email the email to check
     * @return true if exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * Check if a student exists with the given student ID
     * @param studentId the student ID to check
     * @return true if exists, false otherwise
     */
    boolean existsByStudentId(String studentId);

    /**
     * Find a student by email
     * @param email the student's email
     * @return Optional containing the student if found
     */
    Optional<Student> findByEmail(String email);

    /**
     * Find a student by student ID
     * @param studentId the student ID
     * @return Optional containing the student if found
     */
    Optional<Student> findByStudentId(String studentId);

    /**
     * Find all enabled students
     * @return List of enabled students
     */
    java.util.List<Student> findByEnabledTrue();

    /**
     * Count enabled students
     * @return count of enabled students
     */
    long countByEnabledTrue();
}