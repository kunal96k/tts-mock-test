package com.tts.testApp.repository;


import com.tts.testApp.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /**
     * Find student by email
     */
    Optional<Student> findByEmail(String email);

    /**
     * Find student by student ID
     */
    Optional<Student> findByStudentId(String studentId);

    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);

    /**
     * Check if student ID exists
     */
    boolean existsByStudentId(String studentId);

    /**
     * Get the latest student ID number for auto-generation
     */
    @Query("SELECT COUNT(s) FROM Student s")
    long countAllStudents();

    List<Student> findAllByOrderByRegisteredDateDesc();

    long countByEnabledTrue();
}