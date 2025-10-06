package com.tts.testApp.controller;

import com.tts.testApp.dto.SignUpDTO;
import com.tts.testApp.exception.AdminAlreadyExistsException;
import com.tts.testApp.exception.PasswordMismatchException;
import com.tts.testApp.forms.*;
import com.tts.testApp.model.*;
import com.tts.testApp.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PageController {

    private final AdminService adminService;

    @GetMapping("/")
    public String LandingPage() {
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String role, Model model) {

        if (role == null) role = "STUDENT";
        model.addAttribute("role", role.toUpperCase());
        return "login";
    }

    @GetMapping("/signup")
    public String showSignUpForm(Model model) {
        model.addAttribute("signUpDTO", new SignUpDTO());
        return "signup";
    }

    @PostMapping("/signup")
    public String registerAdmin(@Valid @ModelAttribute SignUpDTO signUpDTO,
                                BindingResult bindingResult,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {

        // Validation errors
        if (bindingResult.hasErrors()) {
            // Collect all error messages into one string
            StringBuilder errors = new StringBuilder();
            bindingResult.getAllErrors().forEach(error -> errors.append(error.getDefaultMessage()).append(". "));
            redirectAttributes.addFlashAttribute("error", errors.toString());
            return "redirect:/signup";
        }

        try {
            // Register admin
            Admin admin = adminService.registerAdmin(signUpDTO, request);
            redirectAttributes.addFlashAttribute("success", "Sign up successful! You can now login.");
            return "redirect:/login?role=ADMIN";

        } catch (PasswordMismatchException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/signup";

        } catch (AdminAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/signup";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "An unexpected error occurred. Please try again.");
            return "redirect:/signup";
        }
    }



    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        return "reset-password";
    }

    @GetMapping("/admin-dashboard")
    public String adminDashboardPage(Model model) {

        // Add forms if not present
        if (!model.containsAttribute("studentForm")) {
            model.addAttribute("studentForm", new StudentForm());
        }
        if (!model.containsAttribute("uploadForm")) {
            model.addAttribute("uploadForm", new UploadForm());
        }
        if (!model.containsAttribute("subjectForm")) {
            model.addAttribute("subjectForm", new SubjectForm());
        }

        // Admin name
        model.addAttribute("adminName", "John Doe");

        // Dashboard statistics
        DashboardStats stats = new DashboardStats();
        stats.setTotalStudents(5);
        stats.setTestsCompleted(12);
        stats.setPassRate(75);
        stats.setActiveSubjects(3);
        model.addAttribute("dashboardStats", stats);

        // Recent test results - NOW WITH timeTaken parameter
        List<TestResult> recentResults = new ArrayList<>();
        recentResults.add(new TestResult("STD-001", "Alice Smith", "Java", 5, 30, false,
                LocalDate.now().minusDays(1), 25)); // 45 minutes
        recentResults.add(new TestResult("STD-002", "Bob Johnson", "Python", 11, 30, false,
                LocalDate.now().minusDays(2), 30)); // 30 minutes
        recentResults.add(new TestResult("STD-003", "Charlie Brown", "JavaScript", 30, 30, true,
                LocalDate.now().minusDays(3), 20)); // 50 minutes
        model.addAttribute("recentResults", recentResults);


        // Test results for results section (same as recentResults)
        model.addAttribute("testResults", recentResults);

        // Question Banks
        List<QuestionBank> questionBanks = new ArrayList<>();
        questionBanks.add(new QuestionBank(1L, "Java", "java-questions.csv", 50,
                LocalDate.now().minusDays(5), true));
        questionBanks.add(new QuestionBank(2L, "Python", "python-questions.csv", 30,
                LocalDate.now().minusDays(10), false));
        model.addAttribute("questionBanks", questionBanks);

        // Subjects
        List<Subject> subjects = new ArrayList<>();
        subjects.add(new Subject(1L, "Java", 50, 5));
        subjects.add(new Subject(2L, "Python", 30, 3));
        subjects.add(new Subject(3L, "JavaScript", 0, 0));
        model.addAttribute("allSubjects", subjects);
        model.addAttribute("subjects", subjects); // For dropdowns

        // Analytics
        Analytics analytics = new Analytics();
        analytics.setAvgScore(72);
        analytics.setCompletionRate(85);
        analytics.setAvgTime(45);
        analytics.setTopPerformer("Alice Smith");
        analytics.setTopPerformerScore(95);
        analytics.setScoreChange("+5%");
        analytics.setCompletionChange("+10%");
        analytics.setTimeChange("-3 min");
        model.addAttribute("analytics", analytics);

        // Subject-wise performance
        List<SubjectPerformance> subjectPerformance = new ArrayList<>();
        subjectPerformance.add(new SubjectPerformance("Java", 5, 78, 80, 40));
        subjectPerformance.add(new SubjectPerformance("Python", 3, 65, 50, 50));
        model.addAttribute("subjectPerformance", subjectPerformance);

        return "admin-dashboard";
    }

    @GetMapping("/student-dashboard")
    public String studentDashboardPage() {
        return "student-dashboard";
    }

    @GetMapping("/start-test")
    public String startTestPage() {
        return "start-test";
    }

    @GetMapping("/footer")
    public String footerPage() {
        return "footer";
    }
}