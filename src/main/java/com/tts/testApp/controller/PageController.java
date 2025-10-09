package com.tts.testApp.controller;

import com.tts.testApp.dto.SignUpDTO;
import com.tts.testApp.exception.AdminAlreadyExistsException;
import com.tts.testApp.exception.PasswordMismatchException;
import com.tts.testApp.model.Admin;
import com.tts.testApp.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

@Controller
@RequiredArgsConstructor
@Slf4j
public class PageController {

    private final AdminService adminService;

    // ========================================
    // PUBLIC PAGES
    // ========================================

    @GetMapping("/")
    public String landingPage() {
        log.info("Landing page accessed");
        return "index";
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String role, Model model) {
        if (role == null) {
            role = "STUDENT";
        }
        model.addAttribute("role", role.toUpperCase());
        log.info("Login page accessed with role: {}", role);
        return "login";
    }

    // ========================================
    // ADMIN SIGNUP
    // ========================================

    @GetMapping("/signup")
    public String showSignUpForm(Model model) {
        if (!model.containsAttribute("signUpDTO")) {
            model.addAttribute("signUpDTO", new SignUpDTO());
        }
        log.info("Signup page accessed");
        return "signup";
    }

    @PostMapping("/signup")
    public String registerAdmin(
            @Valid @ModelAttribute SignUpDTO signUpDTO,
            BindingResult bindingResult,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        log.info("Signup attempt for email: {}", signUpDTO.getEmail());

        // Validation errors
        if (bindingResult.hasErrors()) {
            StringBuilder errors = new StringBuilder();
            bindingResult.getAllErrors().forEach(error ->
                    errors.append(error.getDefaultMessage()).append(". ")
            );
            redirectAttributes.addFlashAttribute("error", errors.toString());
            redirectAttributes.addFlashAttribute("signUpDTO", signUpDTO);
            return "redirect:/signup";
        }

        try {
            // ====== ADMIN CODE VALIDATION ======
            String SECRET_ADMIN_CODE = "ZVNabE9HZGpNNGZu";

            if ("ADMIN".equalsIgnoreCase(signUpDTO.getRole())) {
                if (signUpDTO.getAdminCode() == null ||
                        !SECRET_ADMIN_CODE.equals(signUpDTO.getAdminCode().trim())) {
                    redirectAttributes.addFlashAttribute("error", "Invalid Admin Code. Signup failed.");
                    redirectAttributes.addFlashAttribute("signUpDTO", signUpDTO);
                    return "redirect:/signup";
                }
            } else {
                redirectAttributes.addFlashAttribute("error",
                        "Access restricted: This signup service is temporarily available only for administrators.");
                redirectAttributes.addFlashAttribute("signUpDTO", signUpDTO);
                return "redirect:/signup";
            }

            Admin admin = adminService.registerAdmin(signUpDTO, request);
            log.info("Admin registered successfully: {}", admin.getEmail());
            redirectAttributes.addFlashAttribute("success",
                    "Sign up successful! You can now login.");
            return "redirect:/login?role=ADMIN";

        } catch (PasswordMismatchException | AdminAlreadyExistsException e) {
            log.error("Registration failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("signUpDTO", signUpDTO);
            return "redirect:/signup";

        } catch (Exception e) {
            log.error("Unexpected error during registration", e);
            redirectAttributes.addFlashAttribute("error",
                    "An unexpected error occurred. Please try again.");
            redirectAttributes.addFlashAttribute("signUpDTO", signUpDTO);
            return "redirect:/signup";
        }
    }

    // ========================================
    // PASSWORD RESET PAGES
    // ========================================

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        log.info("Forgot password page accessed");
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage() {
        log.info("Reset password page accessed");
        return "reset-password";
    }

    // ========================================
    // STUDENT DASHBOARD
    // ========================================

    @GetMapping("/student-dashboard")
    public String studentDashboardPage() {
        log.info("Student dashboard page accessed");
        return "student-dashboard";
    }

    @GetMapping("/start-test")
    public String startTestPage() {
        log.info("Start test page accessed");
        return "start-test";
    }

    // ========================================
    // FOOTER (if needed as standalone)
    // ========================================

    @GetMapping("/footer")
    public String footerPage() {
        log.info("Footer page accessed");
        return "footer";
    }
}