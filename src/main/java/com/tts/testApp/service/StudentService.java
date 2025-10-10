package com.tts.testApp.service;

import com.tts.testApp.dto.StudentDTO;
import com.tts.testApp.exception.DuplicateResourceException;
import com.tts.testApp.exception.InvalidInputException;
import com.tts.testApp.exception.ResourceNotFoundException;
import com.tts.testApp.model.Student;
import com.tts.testApp.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Student createStudent(StudentDTO studentDTO) {
        log.info("Creating new student with email: {}", studentDTO.getEmail());

        // Validate password confirmation
        if (studentDTO.getPassword() != null && studentDTO.getConfirmPassword() != null) {
            if (!studentDTO.getPassword().equals(studentDTO.getConfirmPassword())) {
                throw new InvalidInputException("Password and confirm password do not match");
            }
        }

        // Check if email already exists
        if (studentRepository.existsByEmail(studentDTO.getEmail())) {
            throw new DuplicateResourceException("Email already exists: " + studentDTO.getEmail());
        }

        // Create new student
        Student student = new Student();
        student.setStudentId(generateStudentId());
        student.setFullName(studentDTO.getFullName().trim());
        student.setEmail(studentDTO.getEmail().toLowerCase().trim());
        student.setPassword(passwordEncoder.encode(studentDTO.getPassword()));
        student.setRole("ROLE_STUDENT");
        student.setEnabled(studentDTO.isEnabled());
        student.setAccountNonLocked(true);
        student.setRegisteredDate(LocalDateTime.now());
        student.setTestsTaken(0);

        Student savedStudent = studentRepository.save(student);
        log.info("Student created successfully with ID: {}", savedStudent.getStudentId());

        return savedStudent;
    }

    public List<StudentDTO> getAllStudents() {
        return studentRepository.findAll().stream()
                .map(StudentDTO::new)
                .collect(Collectors.toList());
    }

    public StudentDTO getStudentById(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        return new StudentDTO(student);
    }

    /**
     * Find student by email
     * @param email the student's email
     * @return Optional containing the student if found
     */
    public Optional<Student> findByEmail(String email) {
        log.debug("Finding student by email: {}", email);
        return studentRepository.findByEmail(email.toLowerCase().trim());
    }

    /**
     * Get student entity by email (throws exception if not found)
     * @param email the student's email
     * @return the student entity
     */
    public Student getStudentByEmail(String email) {
        log.debug("Getting student by email: {}", email);
        return studentRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with email: " + email));
    }

    /**
     * Get student DTO by email
     * @param email the student's email
     * @return StudentDTO
     */
    public StudentDTO getStudentDTOByEmail(String email) {
        Student student = getStudentByEmail(email);
        return new StudentDTO(student);
    }

    @Transactional
    public StudentDTO updateStudent(Long id, StudentDTO studentDTO) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        // Check email uniqueness if changed
        if (!student.getEmail().equals(studentDTO.getEmail())) {
            if (studentRepository.existsByEmail(studentDTO.getEmail())) {
                throw new DuplicateResourceException("Email already exists: " + studentDTO.getEmail());
            }
            student.setEmail(studentDTO.getEmail().toLowerCase().trim());
        }

        // Update fields
        student.setFullName(studentDTO.getFullName().trim());
        student.setEnabled(studentDTO.isEnabled());

        // Update password only if provided
        if (studentDTO.getPassword() != null && !studentDTO.getPassword().isEmpty()) {
            if (studentDTO.getConfirmPassword() != null &&
                    !studentDTO.getPassword().equals(studentDTO.getConfirmPassword())) {
                throw new InvalidInputException("Password and confirm password do not match");
            }
            student.setPassword(passwordEncoder.encode(studentDTO.getPassword()));
        }

        Student updated = studentRepository.save(student);
        log.info("Student updated successfully: {}", updated.getStudentId());

        return new StudentDTO(updated);
    }

    @Transactional
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        studentRepository.delete(student);
        log.info("Student deleted successfully: {}", student.getStudentId());
    }

    public long countStudents() {
        return studentRepository.count();
    }

    /**
     * Increment the test count for a student
     * @param studentId the student's ID
     */
    @Transactional
    public void incrementTestCount(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        student.setTestsTaken(student.getTestsTaken() + 1);
        studentRepository.save(student);
        log.info("Incremented test count for student: {} (Total: {})",
                student.getStudentId(), student.getTestsTaken());
    }

    /**
     * Increment test count by email
     * @param email the student's email
     */
    @Transactional
    public void incrementTestCountByEmail(String email) {
        Student student = getStudentByEmail(email);
        student.setTestsTaken(student.getTestsTaken() + 1);
        studentRepository.save(student);
        log.info("Incremented test count for student: {} (Total: {})",
                student.getStudentId(), student.getTestsTaken());
    }

    private String generateStudentId() {
        String prefix = "STD-";
        Long count = studentRepository.count();
        String number = String.format("%03d", count + 1);

        String studentId = prefix + number;

        // Ensure uniqueness
        while (studentRepository.existsByStudentId(studentId)) {
            count++;
            number = String.format("%03d", count + 1);
            studentId = prefix + number;
        }

        return studentId;
    }

    public List<StudentDTO> searchStudents(String query) {
        log.info("Searching students with query: {}", query);

        String searchTerm = query.toLowerCase().trim();

        return studentRepository.findAll().stream()
                .filter(student ->
                        student.getStudentId().toLowerCase().contains(searchTerm) ||
                                student.getFullName().toLowerCase().contains(searchTerm) ||
                                student.getEmail().toLowerCase().contains(searchTerm))
                .map(StudentDTO::new)
                .collect(Collectors.toList());
    }
}