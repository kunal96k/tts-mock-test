package com.tts.testApp.service;

import com.tts.testApp.dto.CreateTestDTO;
import com.tts.testApp.exception.DuplicateResourceException;
import com.tts.testApp.exception.InvalidInputException;
import com.tts.testApp.exception.ResourceNotFoundException;
import com.tts.testApp.model.CreateTest;
import com.tts.testApp.model.Subject;
import com.tts.testApp.repository.CreateTestRepository;
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
public class CreateTestService {

    private final CreateTestRepository createTestRepository;
    private final SubjectRepository subjectRepository;

    /**
     * Create a new test
     */
    @Transactional
    public CreateTestDTO createTest(CreateTestDTO testDTO) {


        log.info("Creating new test for subject: {}", testDTO.getSubjectName());

        // Validate input
        validateTestDTO(testDTO);

        // Find the subject by name
        Subject subject = subjectRepository.findByName(testDTO.getSubjectName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject not found: " + testDTO.getSubjectName()));

        // Auto-generate testName from subjectName + testType
        String generatedTestName = testDTO.generateTestName();
        testDTO.setTestName(generatedTestName);

        // Check for duplicate test name
        if (createTestRepository.existsByTestName(generatedTestName)) {
            throw new DuplicateResourceException(
                    "Test with name '" + generatedTestName + "' already exists");
        }

        // Convert DTO to Entity
        CreateTest test = new CreateTest();

        test.setSubject(subject); // Set subject relationship
        test.setTestName(generatedTestName);
        test.setTestType(testDTO.getTestType());
        test.setTotalQuestions(testDTO.getTotalQuestions());
        test.setDuration(testDTO.getDuration());
        test.setPassingPercentage(testDTO.getPassingPercentage());
        test.setMarksPerQuestion(testDTO.getMarksPerQuestion());
        test.setTabSwitchLimit(testDTO.getTabSwitchLimit());
        test.setActive(true);

        // Calculate total marks
        int totalMarks = testDTO.getTotalQuestions() * testDTO.getMarksPerQuestion();
        test.setTotalMarks(totalMarks);

        // Timestamps will be set by @PrePersist

        // Save test
        CreateTest savedTest = createTestRepository.save(test);
        log.info("Test created successfully with ID: {} and name: {}",
                savedTest.getId(), savedTest.getTestName());

        return convertToDTO(savedTest);
    }

    /**
     * Get all tests
     */
    @Transactional(readOnly = true)
    public List<CreateTestDTO> getAllTests() {
        log.info("Fetching all tests");
        return createTestRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get active tests only
     */
    @Transactional(readOnly = true)
    public List<CreateTestDTO> getActiveTests() {
        log.info("Fetching active tests");
        return createTestRepository.findByActiveTrue()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }


    /**
     * Get test by ID
     */
    @Transactional(readOnly = true)
    public CreateTestDTO getTestById(Long id) {
        log.info("Fetching test with ID: {}", id);
        CreateTest test = createTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Test not found with ID: " + id));
        return convertToDTO(test);
    }

    /**
     * Get test by name
     */
    @Transactional(readOnly = true)
    public CreateTestDTO getTestByName(String testName) {
        log.info("Fetching test with name: {}", testName);
        CreateTest test = createTestRepository.findByTestName(testName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Test not found with name: " + testName));
        return convertToDTO(test);
    }

    /**
     * Get tests by subject
     */
    @Transactional(readOnly = true)
    public List<CreateTestDTO> getTestsBySubject(String subjectName) {
        log.info("Fetching tests for subject: {}", subjectName);
        Subject subject = subjectRepository.findByName(subjectName)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Subject not found: " + subjectName));

        return createTestRepository.findBySubject(subject)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Update existing test
     */
    @Transactional
    public CreateTestDTO updateTest(Long id, CreateTestDTO testDTO) {
        log.info("Updating test with ID: {}", id);

        CreateTest existingTest = createTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Test not found with ID: " + id));

        // Validate input
        validateTestDTO(testDTO);

        // Update subject if changed
        if (testDTO.getSubjectName() != null) {
            Subject subject = subjectRepository.findByName(testDTO.getSubjectName())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Subject not found: " + testDTO.getSubjectName()));
            existingTest.setSubject(subject);
        }

        // Check for duplicate name (excluding current test)
        String newTestName = testDTO.generateTestName();
        if (!existingTest.getTestName().equals(newTestName)
                && createTestRepository.existsByTestName(newTestName)) {
            throw new DuplicateResourceException(
                    "Test with name '" + newTestName + "' already exists");
        }

        // Update fields
        existingTest.setTestType(testDTO.getTestType());
        existingTest.setTotalQuestions(testDTO.getTotalQuestions());
        existingTest.setDuration(testDTO.getDuration());
        existingTest.setPassingPercentage(testDTO.getPassingPercentage());
        existingTest.setMarksPerQuestion(testDTO.getMarksPerQuestion());
        existingTest.setTabSwitchLimit(testDTO.getTabSwitchLimit());

        // Recalculate total marks
        int totalMarks = testDTO.getTotalQuestions() * testDTO.getMarksPerQuestion();
        existingTest.setTotalMarks(totalMarks);

        // updatedAt will be set by @PreUpdate

        CreateTest updatedTest = createTestRepository.save(existingTest);
        log.info("Test updated successfully with ID: {}", updatedTest.getId());

        return convertToDTO(updatedTest);
    }

    /**
     * Toggle test active status
     */
    @Transactional
    public CreateTestDTO toggleTestStatus(Long id) {
        log.info("Toggling status for test ID: {}", id);

        CreateTest test = createTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Test not found with ID: " + id));

        // Fix: Use getActive() instead of isActive()
        test.setActive(!test.getActive());

        CreateTest updatedTest = createTestRepository.save(test);
        log.info("Test status updated. ID: {}, Active: {}", id, updatedTest.getActive());

        return convertToDTO(updatedTest);
    }

    /**
     * Delete test
     */
    @Transactional
    public void deleteTest(Long id) {
        log.info("Deleting test with ID: {}", id);

        CreateTest test = createTestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Test not found with ID: " + id));

        // You can add additional checks here, e.g., if test has been taken by students
        // if (test.getAttemptCount() > 0) {
        //     throw new InvalidInputException("Cannot delete test that has been attempted by students");
        // }

        createTestRepository.delete(test);
        log.info("Test deleted successfully with ID: {}", id);
    }

    /**
     * Get tests by type (MOCK or FINAL)
     */
    @Transactional(readOnly = true)
    public List<CreateTestDTO> getTestsByType(String testType) {
        log.info("Fetching tests of type: {}", testType);
        return createTestRepository.findByTestType(testType)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Count total tests
     */
    @Transactional(readOnly = true)
    public long countAllTests() {
        return createTestRepository.count();
    }

    /**
     * Count active tests
     */
    @Transactional(readOnly = true)
    public long countActiveTests() {
        return createTestRepository.countByActiveTrue();
    }

    // ==================== Helper Methods ====================

    /**
     * Validate test DTO
     */
    private void validateTestDTO(CreateTestDTO testDTO) {
        if (testDTO == null) {
            throw new InvalidInputException("Test data cannot be null");
        }

        if (testDTO.getSubjectName() == null || testDTO.getSubjectName().trim().isEmpty()) {
            throw new InvalidInputException("Subject name is required");
        }

        if (testDTO.getTestType() == null || testDTO.getTestType().trim().isEmpty()) {
            throw new InvalidInputException("Test type is required");
        }

        if (!testDTO.getTestType().equalsIgnoreCase("MOCK")
                && !testDTO.getTestType().equalsIgnoreCase("FINAL")) {
            throw new InvalidInputException("Test type must be either MOCK or FINAL");
        }

        if (testDTO.getTotalQuestions() == null || testDTO.getTotalQuestions() < 1) {
            throw new InvalidInputException("Total questions must be at least 1");
        }

        if (testDTO.getDuration() == null || testDTO.getDuration() < 5) {
            throw new InvalidInputException("Duration must be at least 5 minutes");
        }

        if (testDTO.getPassingPercentage() == null
                || testDTO.getPassingPercentage() < 1
                || testDTO.getPassingPercentage() > 100) {
            throw new InvalidInputException("Passing percentage must be between 1 and 100");
        }

        if (testDTO.getMarksPerQuestion() == null || testDTO.getMarksPerQuestion() < 1) {
            throw new InvalidInputException("Marks per question must be at least 1");
        }

        if (testDTO.getTabSwitchLimit() == null || testDTO.getTabSwitchLimit() < 0) {
            throw new InvalidInputException("Tab switch limit cannot be negative");
        }

        // Trim and normalize data
        testDTO.setSubjectName(testDTO.getSubjectName().trim());
        testDTO.setTestType(testDTO.getTestType().toUpperCase().trim());
    }

    /**
     * Convert Entity to DTO
     */
    private CreateTestDTO convertToDTO(CreateTest test) {

        CreateTestDTO dto = new CreateTestDTO();

        // CRITICAL FIX in convertToDTO method:
        if (test.getSubject() != null) {
            dto.setSubjectName(test.getSubject().getName());
            dto.setSubjectId(test.getSubject().getId());
        } else {
            log.warn("Test {} has no associated subject!", test.getId());
            dto.setSubjectName("Unknown Subject");
        }


        dto.setId(test.getId());
        dto.setSubjectName(test.getSubject().getName());
        dto.setSubjectId(test.getSubject().getId());
        dto.setTestName(test.getTestName());
        dto.setTestType(test.getTestType());
        dto.setTotalQuestions(test.getTotalQuestions());
        dto.setDuration(test.getDuration());
        dto.setPassingPercentage(test.getPassingPercentage());
        dto.setMarksPerQuestion(test.getMarksPerQuestion());
        dto.setTotalMarks(test.getTotalMarks());
        dto.setTabSwitchLimit(test.getTabSwitchLimit());
        dto.setActive(test.getActive());
        dto.setCreatedAt(test.getCreatedAt());
        dto.setUpdatedAt(test.getUpdatedAt());
        return dto;
    }
}