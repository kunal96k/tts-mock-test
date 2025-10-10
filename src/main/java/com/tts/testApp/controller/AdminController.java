package com.tts.testApp.controller;

import com.tts.testApp.dto.CreateTestDTO;
import com.tts.testApp.dto.QuestionBankDTO;
import com.tts.testApp.dto.StudentDTO;
import com.tts.testApp.dto.SubjectDTO;
import com.tts.testApp.model.Admin;
import com.tts.testApp.model.Student;
import com.tts.testApp.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SubjectService subjectService;
    private final StudentService studentService;
    private final AdminService adminService;
    private final CreateTestService createTestService;
    private final QuestionBankService questionBankService;

    // ========================================
    // DASHBOARD
    // ========================================
    @GetMapping("/dashboard")
    public String showDashboard(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Admin dashboard accessed by: {}",
                userDetails != null ? userDetails.getUsername() : "Unknown");

        try {
            // Fetch data
            List<SubjectDTO> subjects = subjectService.getAllSubjects();
            List<StudentDTO> allStudents = studentService.getAllStudents();
            // Question banks - NEW
            List<QuestionBankDTO> questionBanks =
                    questionBankService.convertToDTOs(questionBankService.getAllQuestionBanks());

            model.addAttribute("questionBanks", questionBanks);

            // Initialize DTOs if not already in model (from flash attributes)
            if (!model.containsAttribute("subjectDTO")) {
                model.addAttribute("subjectDTO", new SubjectDTO());
            }
            if (!model.containsAttribute("studentDTO")) {
                model.addAttribute("studentDTO", new StudentDTO());
            }
            if (!model.containsAttribute("questionBankDTO")) {
                model.addAttribute("questionBankDTO", new QuestionBankDTO());
            }
            if (!model.containsAttribute("testDTO")) {
                model.addAttribute("testDTO", new CreateTestDTO());
            }

            model.addAttribute("subjectsCount", subjects.size());

            // Add subject test names for dropdown
            List<String> subjectTestNames = subjects.stream()
                    .map(SubjectDTO::getName)
                    .collect(Collectors.toList());
            model.addAttribute("subjectTestNames", subjectTestNames);

            log.info("Question banks count: {}", questionBanks.size());
            questionBanks.forEach(qb -> log.info("QB: {} / {}", qb.getFileName(), qb.getSubjectName()));

            // Add to model
            model.addAttribute("subjects", subjects);
            model.addAttribute("allSubjects", subjects);
            model.addAttribute("allStudents", allStudents);
            // All tests - FOR CREATE TEST SECTION TABLE
            model.addAttribute("allTests", createTestService.getAllTests());

            // Add admin name
            if (userDetails != null) {
                String username = userDetails.getUsername();

                // Fetch the admin user by email
                Optional<Admin> adminOpt = adminService.findByUsername(username);

                String firstName = "Admin";

                if (adminOpt.isPresent()) {
                    String fullName = adminOpt.get().getFullName().trim();

                    // Extract first name
                    if (!fullName.isEmpty()) {
                        firstName = fullName.split("\\s+")[0]; // split by space and take first part
                    }
                }

                model.addAttribute("adminName", firstName);
            }

            // Add placeholders for other dashboard data
            model.addAttribute("recentResults", List.of());
            model.addAttribute("testResults", List.of());
            model.addAttribute("dashboardStats", createDashboardStats());
            model.addAttribute("analytics", createAnalytics());
            model.addAttribute("subjectPerformance", List.of());

            log.info("Dashboard loaded with {} subjects and {} students",
                    subjects.size(), allStudents.size());

        } catch (Exception e) {
            log.error("Error loading dashboard", e);
            model.addAttribute("error", "Failed to load dashboard data");

            // Initialize empty DTOs to prevent binding errors
            if (!model.containsAttribute("subjectDTO")) {
                model.addAttribute("subjectDTO", new SubjectDTO());
            }
            if (!model.containsAttribute("studentDTO")) {
                model.addAttribute("studentDTO", new StudentDTO());
            }
        }

        return "admin-dashboard";
    }

    // ========================================
    // STUDENT MANAGEMENT (UI)
    // ========================================

    // Search students
    @GetMapping("/students/search")
    public ResponseEntity<List<StudentDTO>> searchStudents(@RequestParam String query) {
        try {
            List<StudentDTO> students = studentService.searchStudents(query);
            return ResponseEntity.ok(students);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/students/add")
    public String addStudent(
            @Valid @ModelAttribute("studentDTO") StudentDTO studentDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        log.info("Received request to add student: {}", studentDTO.getEmail());

        if (bindingResult.hasErrors()) {
            String firstError = bindingResult.getFieldErrors().getFirst().getDefaultMessage();
            redirectAttributes.addFlashAttribute("studentError", firstError);
            redirectAttributes.addFlashAttribute("studentDTO", studentDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.studentDTO", bindingResult);
            return "redirect:/admin/dashboard";
        }

        try {
            Student student = studentService.createStudent(studentDTO);
            redirectAttributes.addFlashAttribute("success",
                    "Student created successfully! Student ID: " + student.getStudentId());
            log.info("Student added successfully: {}", student.getStudentId());
        } catch (Exception e) {
            log.error("Error adding student: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("studentError", e.getMessage());
            redirectAttributes.addFlashAttribute("studentDTO", studentDTO);
        }

        return "redirect:/admin/dashboard#students";
    }

    @PostMapping("/students/{id}/update")
    public String updateStudent(
            @PathVariable Long id,
            @Valid @ModelAttribute("studentDTO") StudentDTO studentDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        log.info("Updating student with ID: {}", id);

        if (bindingResult.hasErrors()) {
            String firstError = bindingResult.getFieldErrors().get(0).getDefaultMessage();
            redirectAttributes.addFlashAttribute("studentError", firstError);
            redirectAttributes.addFlashAttribute("studentDTO", studentDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.studentDTO", bindingResult);
            return "redirect:/admin/dashboard#students";
        }

        try {
            StudentDTO updated = studentService.updateStudent(id, studentDTO);
            redirectAttributes.addFlashAttribute("success",
                    "Student '" + updated.getFullName() + "' updated successfully!");
            log.info("Student updated successfully: {}", updated.getFullName());
        } catch (Exception e) {
            log.error("Error updating student", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("studentDTO", studentDTO);
        }

        return "redirect:/admin/dashboard#students";
    }

    @PostMapping("/students/{id}/delete")
    public String deleteStudent(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        log.info("Deleting student with ID: {}", id);

        try {
            studentService.deleteStudent(id);
            redirectAttributes.addFlashAttribute("success", "Student deleted successfully");
            log.info("Student deleted: {}", id);
        } catch (Exception e) {
            log.error("Error deleting student: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/dashboard#students";
    }

    // ========================================
    // STUDENT REST API
    // ========================================
    @PostMapping("/api/students")
    @ResponseBody
    public StudentDTO createStudentAPI(@Valid @RequestBody StudentDTO studentDTO) {
        log.info("API: Creating student: {}", studentDTO.getEmail());
        Student student = studentService.createStudent(studentDTO);
        return new StudentDTO(student);
    }

    @GetMapping("/api/students")
    @ResponseBody
    public List<StudentDTO> getAllStudentsAPI() {
        log.info("API: Fetching all students");
        return studentService.getAllStudents();
    }

    @GetMapping("/api/students/{id}")
    @ResponseBody
    public StudentDTO getStudentAPI(@PathVariable Long id) {
        log.info("API: Fetching student with ID: {}", id);
        return studentService.getStudentById(id);
    }

    @PutMapping("/api/students/{id}")
    @ResponseBody
    public StudentDTO updateStudentAPI(
            @PathVariable Long id,
            @Valid @RequestBody StudentDTO studentDTO) {
        log.info("API: Updating student with ID: {}", id);
        return studentService.updateStudent(id, studentDTO);
    }

    @DeleteMapping("/api/students/{id}")
    @ResponseBody
    public void deleteStudentAPI(@PathVariable Long id) {
        log.info("API: Deleting student with ID: {}", id);
        studentService.deleteStudent(id);
    }

    // ========================================
    // SUBJECT MANAGEMENT (UI)
    // ========================================
    @PostMapping("/subjects/add")
    public String addSubject(
            @Valid @ModelAttribute("subjectDTO") SubjectDTO subjectDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        log.info("Adding new subject: {}", subjectDTO.getName());

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldError() != null
                    ? bindingResult.getFieldError().getDefaultMessage()
                    : "Invalid input data";
            redirectAttributes.addFlashAttribute("subjectError", errorMessage);
            redirectAttributes.addFlashAttribute("subjectDTO", subjectDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.subjectDTO", bindingResult);
            return "redirect:/admin/dashboard#subjects";
        }

        try {
            SubjectDTO createdSubject = subjectService.createSubject(subjectDTO);
            redirectAttributes.addFlashAttribute("success",
                    "Subject '" + createdSubject.getName() + "' created successfully!");
            log.info("Subject created successfully: {}", createdSubject.getName());
        } catch (Exception e) {
            log.error("Error creating subject", e);
            redirectAttributes.addFlashAttribute("subjectError", e.getMessage());
            redirectAttributes.addFlashAttribute("subjectDTO", subjectDTO);
        }

        return "redirect:/admin/dashboard#subjects";
    }

    @PostMapping("/subjects/{id}/update")
    public String updateSubject(
            @PathVariable Long id,
            @Valid @ModelAttribute SubjectDTO subjectDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        log.info("Updating subject with ID: {}", id);

        if (bindingResult.hasErrors()) {
            String errorMessage = bindingResult.getFieldError() != null
                    ? bindingResult.getFieldError().getDefaultMessage()
                    : "Invalid input data";
            redirectAttributes.addFlashAttribute("subjectError", errorMessage);
            redirectAttributes.addFlashAttribute("subjectDTO", subjectDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.subjectDTO", bindingResult);
            return "redirect:/admin/dashboard#subjects";
        }

        try {
            SubjectDTO updatedSubject = subjectService.updateSubject(id, subjectDTO);
            redirectAttributes.addFlashAttribute("success",
                    "Subject '" + updatedSubject.getName() + "' updated successfully!");
            log.info("Subject updated successfully: {}", updatedSubject.getName());
        } catch (Exception e) {
            log.error("Error updating subject", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("subjectDTO", subjectDTO);
        }

        return "redirect:/admin/dashboard#subjects";
    }

    @PostMapping("/subjects/{id}/delete")
    public String deleteSubject(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        log.info("Deleting subject with ID: {}", id);

        try {
            SubjectDTO subject = subjectService.getSubjectById(id);
            subjectService.deleteSubject(id);
            redirectAttributes.addFlashAttribute("success",
                    "Subject '" + subject.getName() + "' deleted successfully!");
            log.info("Subject deleted successfully with ID: {}", id);
        } catch (Exception e) {
            log.error("Error deleting subject", e);
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/dashboard#subjects";
    }

    // ========================================
    // SUBJECT REST API
    // ========================================
    @PostMapping("/api/subjects")
    @ResponseBody
    public SubjectDTO createSubjectAPI(@Valid @RequestBody SubjectDTO subjectDTO) {
        log.info("API: Creating subject: {}", subjectDTO.getName());
        return subjectService.createSubject(subjectDTO);
    }

    @GetMapping("/api/subjects")
    @ResponseBody
    public List<SubjectDTO> getAllSubjectsAPI() {
        log.info("API: Fetching all subjects");
        return subjectService.getAllSubjects();
    }

    @GetMapping("/api/subjects/{id}")
    @ResponseBody
    public SubjectDTO getSubjectAPI(@PathVariable Long id) {
        log.info("API: Fetching subject with ID: {}", id);
        return subjectService.getSubjectById(id);
    }

    @PutMapping("/api/subjects/{id}")
    @ResponseBody
    public SubjectDTO updateSubjectAPI(
            @PathVariable Long id,
            @Valid @RequestBody SubjectDTO subjectDTO) {
        log.info("API: Updating subject with ID: {}", id);
        return subjectService.updateSubject(id, subjectDTO);
    }

    @DeleteMapping("/api/subjects/{id}")
    @ResponseBody
    public void deleteSubjectAPI(@PathVariable Long id) {
        log.info("API: Deleting subject with ID: {}", id);
        subjectService.deleteSubject(id);
    }

    // ========================================
    // HELPER METHODS
    // ========================================
    private Object createDashboardStats() {
        return new Object() {
            public long getTotalStudents() {
                return studentService.countStudents();
            }

            public int getTestsCompleted() {
                return 0;
            }

            public int getPassRate() {
                return 0;
            }

            public long getActiveSubjects() {
                return subjectService.countActiveSubjects();
            }
        };
    }

    private Object createAnalytics() {
        return new Object() {
            public int getAvgScore() { return 0; }
            public String getScoreChange() { return "+0% this month"; }
            public int getCompletionRate() { return 0; }
            public String getCompletionChange() { return "+0% this month"; }
            public int getAvgTime() { return 0; }
            public String getTimeChange() { return "-0 min this month"; }
            public String getTopPerformer() { return "N/A"; }
            public int getTopPerformerScore() { return 0; }
        };
    }
}