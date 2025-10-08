package com.tts.testApp.service;

import com.tts.testApp.dto.SubjectDTO;
import com.tts.testApp.exception.DuplicateResourceException;
import com.tts.testApp.exception.InvalidInputException;
import com.tts.testApp.exception.ResourceNotFoundException;
import com.tts.testApp.model.Subject;
import com.tts.testApp.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    @Transactional
    public SubjectDTO createSubject(SubjectDTO subjectDTO) {
        log.info("Creating new subject: {}", subjectDTO.getName());

        // Validate input
        validateSubjectDTO(subjectDTO);

        // Check for duplicates
        if (subjectRepository.existsByName(subjectDTO.getName())) {
            throw new DuplicateResourceException(
                    "Subject with name '" + subjectDTO.getName() + "' already exists");
        }

        if (subjectRepository.existsBySubjectCode(subjectDTO.getSubjectCode())) {
            throw new DuplicateResourceException(
                    "Subject with code '" + subjectDTO.getSubjectCode() + "' already exists");
        }

        // Convert DTO to Entity
        Subject subject = convertToEntity(subjectDTO);

        // Save subject
        Subject savedSubject = subjectRepository.save(subject);
        log.info("Subject created successfully with ID: {}", savedSubject.getId());

        return convertToDTO(savedSubject);
    }

    @Transactional(readOnly = true)
    public List<SubjectDTO> getAllSubjects() {
        log.info("Fetching all subjects");
        return subjectRepository.findAllOrderByCreatedAtDesc()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubjectDTO> getActiveSubjects() {
        log.info("Fetching active subjects");
        return subjectRepository.findByActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SubjectDTO getSubjectById(Long id) {
        log.info("Fetching subject with ID: {}", id);
        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject not found with ID: " + id));
        return convertToDTO(subject);
    }

    @Transactional
    public SubjectDTO updateSubject(Long id, SubjectDTO subjectDTO) {
        log.info("Updating subject with ID: {}", id);

        Subject existingSubject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject not found with ID: " + id));

        // Validate input
        validateSubjectDTO(subjectDTO);

        // Check for duplicate name (excluding current subject)
        if (!existingSubject.getName().equals(subjectDTO.getName())
                && subjectRepository.existsByName(subjectDTO.getName())) {
            throw new DuplicateResourceException(
                    "Subject with name '" + subjectDTO.getName() + "' already exists");
        }

        // Check for duplicate code (excluding current subject)
        if (!existingSubject.getSubjectCode().equals(subjectDTO.getSubjectCode())
                && subjectRepository.existsBySubjectCode(subjectDTO.getSubjectCode())) {
            throw new DuplicateResourceException(
                    "Subject with code '" + subjectDTO.getSubjectCode() + "' already exists");
        }

        // Update fields
        existingSubject.setName(subjectDTO.getName());
        existingSubject.setSubjectCode(subjectDTO.getSubjectCode());
        existingSubject.setDescription(subjectDTO.getDescription());
        existingSubject.setActive(subjectDTO.isActive());

        Subject updatedSubject = subjectRepository.save(existingSubject);
        log.info("Subject updated successfully with ID: {}", updatedSubject.getId());

        return convertToDTO(updatedSubject);
    }

    @Transactional
    public void deleteSubject(Long id) {
        log.info("Deleting subject with ID: {}", id);

        Subject subject = subjectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject not found with ID: " + id));

        // Check if subject has questions or students
        if (subject.getTotalQuestions() > 0) {
            throw new InvalidInputException(
                    "Cannot delete subject '" + subject.getName() +
                            "' as it has " + subject.getTotalQuestions() + " questions associated with it");
        }

        if (subject.getStudentsEnrolled() > 0) {
            throw new InvalidInputException(
                    "Cannot delete subject '" + subject.getName() +
                            "' as it has " + subject.getStudentsEnrolled() + " students enrolled");
        }

        subjectRepository.delete(subject);
        log.info("Subject deleted successfully with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public long countActiveSubjects() {
        return subjectRepository.countByActiveTrue();
    }

    // Helper methods
    private void validateSubjectDTO(SubjectDTO subjectDTO) {
        if (subjectDTO == null) {
            throw new InvalidInputException("Subject data cannot be null");
        }

        if (subjectDTO.getName() == null || subjectDTO.getName().trim().isEmpty()) {
            throw new InvalidInputException("Subject name is required");
        }

        if (subjectDTO.getSubjectCode() == null || subjectDTO.getSubjectCode().trim().isEmpty()) {
            throw new InvalidInputException("Subject code is required");
        }

        // Ensure subject code is uppercase
        subjectDTO.setSubjectCode(subjectDTO.getSubjectCode().toUpperCase().trim());
        subjectDTO.setName(subjectDTO.getName().trim());

        if (subjectDTO.getDescription() != null) {
            subjectDTO.setDescription(subjectDTO.getDescription().trim());
        }
    }

    private Subject convertToEntity(SubjectDTO dto) {
        Subject subject = new Subject();
        subject.setId(dto.getId());
        subject.setName(dto.getName());
        subject.setSubjectCode(dto.getSubjectCode());
        subject.setDescription(dto.getDescription());
        subject.setActive(dto.isActive());
        subject.setTotalQuestions(dto.getTotalQuestions());
        subject.setStudentsEnrolled(dto.getStudentsEnrolled());
        return subject;
    }

    private SubjectDTO convertToDTO(Subject subject) {
        SubjectDTO dto = new SubjectDTO();
        dto.setId(subject.getId());
        dto.setName(subject.getName());
        dto.setSubjectCode(subject.getSubjectCode());
        dto.setDescription(subject.getDescription());
        dto.setActive(subject.isActive());
        dto.setTotalQuestions(subject.getTotalQuestions());
        dto.setStudentsEnrolled(subject.getStudentsEnrolled());
        return dto;
    }
}