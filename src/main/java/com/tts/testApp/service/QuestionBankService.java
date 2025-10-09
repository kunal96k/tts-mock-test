package com.tts.testApp.service;

import com.tts.testApp.dto.CSVQuestionDTO;
import com.tts.testApp.dto.QuestionBankDTO;
import com.tts.testApp.exception.QuestionBankNotFoundException;
import com.tts.testApp.exception.SubjectNotFoundException;
import com.tts.testApp.model.Question;
import com.tts.testApp.model.QuestionBank;
import com.tts.testApp.model.Subject;
import com.tts.testApp.repository.QuestionBankRepository;
import com.tts.testApp.repository.QuestionRepository;
import com.tts.testApp.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuestionBankService {

    private final QuestionBankRepository questionBankRepository;
    private final QuestionRepository questionRepository;
    private final SubjectRepository subjectRepository;
    private final CSVParserService csvParserService;

    @Value("${app.upload.dir:${user.home}/uploads/question-banks}")
    private String uploadDir;

    /**
     * Upload and process question bank CSV file
     */
    @Transactional
    public QuestionBank uploadQuestionBank(QuestionBankDTO dto, String uploadedBy) throws IOException {
        log.info("Starting question bank upload for subject ID: {}", dto.getSubjectId());

        // Validate file
        MultipartFile file = dto.getFile();
        csvParserService.validateCSVFile(file);

        // Get subject
        Subject subject = subjectRepository.findById(dto.getSubjectId())
                .orElseThrow(() -> new SubjectNotFoundException("Subject not found with ID: " + dto.getSubjectId()));

        // Parse CSV
        List<CSVQuestionDTO> csvQuestions = csvParserService.parseCSV(file);

        if (csvQuestions.isEmpty()) {
            throw new IOException("No valid questions found in the uploaded file");
        }

        // Save file to disk
        String savedFileName = saveFile(file);
        Path filePath = Paths.get(uploadDir, savedFileName);

        // Create QuestionBank entity
        QuestionBank questionBank = new QuestionBank();
        questionBank.setFileName(savedFileName);
        questionBank.setOriginalFileName(file.getOriginalFilename());
        questionBank.setFilePath(filePath.toString());
        questionBank.setSubject(subject);
        questionBank.setTotalQuestions(csvQuestions.size());
        questionBank.setActive(true);
        questionBank.setDescription(dto.getDescription());
        questionBank.setFileSize(file.getSize());
        questionBank.setUploadedBy(uploadedBy);

        // Save question bank
        questionBank = questionBankRepository.save(questionBank);
        log.info("Question bank saved with ID: {}", questionBank.getId());

        // Create and save questions
        QuestionBank finalQuestionBank = questionBank;
        List<Question> questions = csvQuestions.stream()
                .map(csvQ -> createQuestion(csvQ, finalQuestionBank))
                .collect(Collectors.toList());

        questionRepository.saveAll(questions);
        log.info("Saved {} questions for question bank ID: {}", questions.size(), questionBank.getId());

        // Update subject's total questions count
        updateSubjectQuestionCount(subject);

        return questionBank;
    }

    private Question createQuestion(CSVQuestionDTO csvQuestion, QuestionBank questionBank) {
        Question question = new Question();
        question.setQuestionText(csvQuestion.getQuestionText());
        question.setOptionA(csvQuestion.getOptionA());
        question.setOptionB(csvQuestion.getOptionB());
        question.setOptionC(csvQuestion.getOptionC());
        question.setOptionD(csvQuestion.getOptionD());
        question.setCorrectAnswer(csvQuestion.getCorrectAnswer());
        question.setExplanation(csvQuestion.getExplanation());
        question.setMarks(csvQuestion.getMarks() != null ? csvQuestion.getMarks() : 1);
        question.setDifficultyLevel(csvQuestion.getDifficultyLevel());
        question.setQuestionBank(questionBank);
        question.setActive(true);
        return question;
    }

    private String saveFile(MultipartFile file) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
            log.info("Created upload directory: {}", uploadPath);
        }

        // Generate unique filename
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".csv";
        String newFileName = "QB_" + timestamp + "_" + uuid + extension;

        // Save file
        Path filePath = uploadPath.resolve(newFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        log.info("File saved: {}", filePath);

        return newFileName;
    }

    private void updateSubjectQuestionCount(Subject subject) {
        Integer totalQuestions = questionBankRepository.sumTotalQuestionsBySubjectId(subject.getId());
        subject.setTotalQuestions(totalQuestions != null ? totalQuestions : 0);
        subjectRepository.save(subject);
        log.info("Updated subject {} total questions: {}", subject.getId(), subject.getTotalQuestions());
    }

    /**
     * Get all question banks
     */
    public List<QuestionBank> getAllQuestionBanks() {
        return questionBankRepository.findAll();
    }

    /**
     * Get question banks by subject
     */
    public List<QuestionBank> getQuestionBanksBySubjectId(Long subjectId) {
        return questionBankRepository.findBySubjectIdOrderByLastUpdatedDesc(subjectId);
    }

    /**
     * Get active question banks
     */
    public List<QuestionBank> getActiveQuestionBanks() {
        return questionBankRepository.findByActiveTrue();
    }

    /**
     * Get question bank by ID
     */
    public QuestionBank getQuestionBankById(Long id) {
        return questionBankRepository.findById(id)
                .orElseThrow(() -> new QuestionBankNotFoundException("Question bank not found with ID: " + id));
    }

    /**
     * Delete question bank and associated questions
     */
    @Transactional
    public void deleteQuestionBank(Long id) {
        log.info("Deleting question bank with ID: {}", id);

        QuestionBank questionBank = getQuestionBankById(id);
        Subject subject = questionBank.getSubject();

        // Delete associated questions
        questionRepository.deleteByQuestionBankId(id);
        log.info("Deleted questions for question bank ID: {}", id);

        // Delete file from disk
        try {
            Path filePath = Paths.get(questionBank.getFilePath());
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", filePath);
        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage());
        }

        // Delete question bank
        questionBankRepository.delete(questionBank);
        log.info("Deleted question bank ID: {}", id);

        // Update subject's total questions count
        updateSubjectQuestionCount(subject);
    }

    /**
     * Toggle question bank active status
     */
    @Transactional
    public void toggleQuestionBankStatus(Long id) {
        QuestionBank questionBank = getQuestionBankById(id);
        questionBank.setActive(!questionBank.isActive());
        questionBankRepository.save(questionBank);
        log.info("Toggled question bank {} status to: {}", id, questionBank.isActive());
    }

    /**
     * Get questions by question bank ID
     */
    public List<Question> getQuestionsByQuestionBankId(Long questionBankId) {
        return questionRepository.findByQuestionBankId(questionBankId);
    }

    /**
     * Convert QuestionBank to DTO
     */
    public QuestionBankDTO convertToDTO(QuestionBank questionBank) {
        QuestionBankDTO dto = new QuestionBankDTO();
        dto.setId(questionBank.getId());
        dto.setSubjectId(questionBank.getSubject().getId());
        dto.setSubjectName(questionBank.getSubject().getName());
        dto.setFileName(questionBank.getFileName());
        dto.setOriginalFileName(questionBank.getOriginalFileName());
        dto.setTotalQuestions(questionBank.getTotalQuestions());
        dto.setActive(questionBank.isActive());
        dto.setFileSize(questionBank.getFileSize());
        dto.setLastUpdated(questionBank.getLastUpdated());
        dto.setUploadedBy(questionBank.getUploadedBy());
        dto.setDescription(questionBank.getDescription());
        return dto;
    }

    /**
     * Convert list of QuestionBanks to DTOs
     */
    public List<QuestionBankDTO> convertToDTOs(List<QuestionBank> questionBanks) {
        return questionBanks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
}