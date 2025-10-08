package com.tts.testApp.controller;

import com.tts.testApp.dto.QuestionBankDTO;
import com.tts.testApp.exception.FileUploadException;
import com.tts.testApp.exception.InvalidCSVFormatException;
import com.tts.testApp.exception.QuestionBankNotFoundException;
import com.tts.testApp.exception.SubjectNotFoundException;
import com.tts.testApp.model.QuestionBank;
import com.tts.testApp.service.QuestionBankService;
import com.tts.testApp.service.SubjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/admin/questions")
@RequiredArgsConstructor
@Slf4j
public class AdminQuestionBankController {

    private final QuestionBankService questionBankService;
    private final SubjectService subjectService;

    /**
     * Upload Question Bank - POST
     */
    @PostMapping("/upload")
    public String uploadQuestionBank(
            @Valid @ModelAttribute QuestionBankDTO questionBankDTO,
            BindingResult bindingResult,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        log.info("Question bank upload request received from: {}", authentication.getName());

        try {
            // Manual validation for file
            MultipartFile file = questionBankDTO.getFile();

            if (file == null || file.isEmpty()) {
                redirectAttributes.addFlashAttribute("uploadError",
                        "Please select a CSV file to upload");
                redirectAttributes.addFlashAttribute("questionBankDTO", questionBankDTO);
                return "redirect:/admin/dashboard#questions-section";
            }

            if (!questionBankDTO.isValidFile()) {
                redirectAttributes.addFlashAttribute("uploadError",
                        "Only CSV files are allowed");
                redirectAttributes.addFlashAttribute("questionBankDTO", questionBankDTO);
                return "redirect:/admin/dashboard#questions-section";
            }

            if (!questionBankDTO.isFileSizeValid()) {
                redirectAttributes.addFlashAttribute("uploadError",
                        "File size must not exceed 5MB");
                redirectAttributes.addFlashAttribute("questionBankDTO", questionBankDTO);
                return "redirect:/admin/dashboard#questions-section";
            }

            // Validation errors
            if (bindingResult.hasErrors()) {
                StringBuilder errors = new StringBuilder();
                bindingResult.getAllErrors().forEach(error ->
                        errors.append(error.getDefaultMessage()).append(". ")
                );
                redirectAttributes.addFlashAttribute("uploadError", errors.toString());
                redirectAttributes.addFlashAttribute("questionBankDTO", questionBankDTO);
                return "redirect:/admin/dashboard#questions-section";
            }

            // Upload and process
            QuestionBank questionBank = questionBankService.uploadQuestionBank(
                    questionBankDTO,
                    authentication.getName()
            );

            log.info("Question bank uploaded successfully: {}", questionBank.getId());
            redirectAttributes.addFlashAttribute("success",
                    String.format("Successfully uploaded %d questions for %s",
                            questionBank.getTotalQuestions(),
                            questionBank.getSubject().getName()));

            return "redirect:/admin/dashboard#questions-section";

        } catch (SubjectNotFoundException e) {
            log.error("Subject not found: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("uploadError",
                    "Selected subject not found. Please select a valid subject.");
            redirectAttributes.addFlashAttribute("questionBankDTO", questionBankDTO);
            return "redirect:/admin/dashboard#questions-section";

        } catch (InvalidCSVFormatException e) {
            log.error("Invalid CSV format: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("uploadError",
                    "Invalid CSV format: " + e.getMessage());
            redirectAttributes.addFlashAttribute("questionBankDTO", questionBankDTO);
            return "redirect:/admin/dashboard#questions-section";

        } catch (IOException e) {
            log.error("File upload error: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("uploadError",
                    "Error uploading file: " + e.getMessage());
            redirectAttributes.addFlashAttribute("questionBankDTO", questionBankDTO);
            return "redirect:/admin/dashboard#questions-section";

        } catch (Exception e) {
            log.error("Unexpected error during question bank upload", e);
            redirectAttributes.addFlashAttribute("uploadError",
                    "An unexpected error occurred. Please try again.");
            redirectAttributes.addFlashAttribute("questionBankDTO", questionBankDTO);
            return "redirect:/admin/dashboard#questions-section";
        }
    }

    /**
     * Delete Question Bank - POST (Using POST with _method hidden field for DELETE)
     */
    @PostMapping("/delete/{id}")
    public String deleteQuestionBank(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        log.info("Delete question bank request for ID: {}", id);

        try {
            questionBankService.deleteQuestionBank(id);
            log.info("Question bank deleted successfully: {}", id);
            redirectAttributes.addFlashAttribute("success",
                    "Question bank deleted successfully");
            return "redirect:/admin/dashboard#questions-section";

        } catch (QuestionBankNotFoundException e) {
            log.error("Question bank not found: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Question bank not found");
            return "redirect:/admin/dashboard#questions-section";

        } catch (Exception e) {
            log.error("Error deleting question bank", e);
            redirectAttributes.addFlashAttribute("error",
                    "Error deleting question bank. Please try again.");
            return "redirect:/admin/dashboard#questions-section";
        }
    }

    /**
     * Toggle Question Bank Status - POST
     */
    @PostMapping("/toggle/{id}")
    public String toggleQuestionBankStatus(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        log.info("Toggle question bank status request for ID: {}", id);

        try {
            questionBankService.toggleQuestionBankStatus(id);
            log.info("Question bank status toggled successfully: {}", id);
            redirectAttributes.addFlashAttribute("success",
                    "Question bank status updated successfully");
            return "redirect:/admin/dashboard#questions-section";

        } catch (QuestionBankNotFoundException e) {
            log.error("Question bank not found: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Question bank not found");
            return "redirect:/admin/dashboard#questions-section";

        } catch (Exception e) {
            log.error("Error toggling question bank status", e);
            redirectAttributes.addFlashAttribute("error",
                    "Error updating question bank status. Please try again.");
            return "redirect:/admin/dashboard#questions-section";
        }
    }
}