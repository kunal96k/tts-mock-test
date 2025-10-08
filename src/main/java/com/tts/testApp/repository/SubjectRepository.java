package com.tts.testApp.repository;

import com.tts.testApp.model.Subject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByName(String name);

    Optional<Subject> findBySubjectCode(String subjectCode);

    boolean existsByName(String name);

    boolean existsBySubjectCode(String subjectCode);

    List<Subject> findByActiveTrue();

    @Query("SELECT s FROM Subject s ORDER BY s.createdAt DESC")
    List<Subject> findAllOrderByCreatedAtDesc();

    long countByActiveTrue();
}