package com.tts.testApp.controller;

import com.tts.testApp.dto.CreateTestDTO;
import com.tts.testApp.service.CreateTestService;
import com.tts.testApp.service.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/student")
@RequiredArgsConstructor
public class StudentController {

    private final CreateTestService createTestService;
    private final StudentService studentService;

    /**
     * Student Dashboard - Shows available tests
     */
    @GetMapping("/dashboard")
    public String showStudentDashboard(
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("========================================");
        log.info("Student Dashboard Access");
        log.info("========================================");
        log.info("User: {}", userDetails != null ? userDetails.getUsername() : "Anonymous");
        log.info("Timestamp: {}", java.time.LocalDateTime.now());

        try {
            // Fetch all active tests
            List<CreateTestDTO> allTests = createTestService.getAllActiveTests();

            log.info("Fetched {} active tests from database", allTests.size());

            // Log each test for debugging
            allTests.forEach(test -> {
                log.debug("Test ID: {}, Name: {}, Subject: {}, Type: {}, Questions: {}, Duration: {} min",
                        test.getId(),
                        test.getTestName(),
                        test.getSubjectName(),
                        test.getTestType(),
                        test.getTotalQuestions(),
                        test.getDuration()
                );
            });

            // Add to model
            model.addAttribute("allTests", allTests);
            model.addAttribute("totalTests", allTests.size());

            // Get student name
            if (userDetails != null) {
                String email = userDetails.getUsername();
                var student = studentService.findByEmail(email);
                if (student.isPresent()) {
                    String fullName = student.get().getFullName();
                    String firstName = fullName != null && !fullName.isEmpty()
                            ? fullName.split(" ")[0]
                            : "Student User";

                    model.addAttribute("studentName", firstName);
                    log.info("Student logged in: {} ({})", student.get().getFullName(), email);
                } else {
                    model.addAttribute("studentName", "Student");
                    log.warn("Student not found for email: {}", email);
                }
            }

            // Stats (you can enhance this)
            model.addAttribute("completedTests", 0);
            model.addAttribute("avgScore", 0);
            model.addAttribute("totalHours", 0);

            log.info("Dashboard loaded successfully for user: {}",
                    userDetails != null ? userDetails.getUsername() : "Anonymous");
            log.info("========================================");

        } catch (Exception e) {
            log.error("========================================");
            log.error("ERROR Loading Student Dashboard");
            log.error("========================================");
            log.error("User: {}", userDetails != null ? userDetails.getUsername() : "Anonymous");
            log.error("Error message: {}", e.getMessage());
            log.error("Stack trace:", e);
            log.error("========================================");

            model.addAttribute("error", "Failed to load dashboard. Please try again.");
            model.addAttribute("allTests", List.of());
        }

        return "student-dashboard";
    }

    /**
     * API: Get all available tests
     */
    @GetMapping("/api/tests")
    @ResponseBody
    public ResponseEntity<List<CreateTestDTO>> getAvailableTests(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("========================================");
        log.info("API: Fetch Available Tests");
        log.info("========================================");
        log.info("Endpoint: GET /student/api/tests");
        log.info("User: {}", userDetails != null ? userDetails.getUsername() : "Anonymous");
        log.info("Timestamp: {}", java.time.LocalDateTime.now());

        try {
            List<CreateTestDTO> tests = createTestService.getAllActiveTests();

            log.info("API Response: {} tests found", tests.size());
            log.debug("Tests: {}", tests);
            log.info("========================================");

            return ResponseEntity.ok(tests);

        } catch (Exception e) {
            log.error("========================================");
            log.error("API ERROR: Failed to fetch tests");
            log.error("========================================");
            log.error("Error: {}", e.getMessage(), e);
            log.error("========================================");

            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * API: Get test by ID
     */
    @GetMapping("/api/tests/{id}")
    @ResponseBody
    public ResponseEntity<CreateTestDTO> getTestById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("========================================");
        log.info("API: Fetch Test by ID");
        log.info("========================================");
        log.info("Endpoint: GET /student/api/tests/{}", id);
        log.info("User: {}", userDetails != null ? userDetails.getUsername() : "Anonymous");
        log.info("Test ID requested: {}", id);

        try {
            CreateTestDTO test = createTestService.getTestById(id);

            log.info("API Response: Test found - {}", test.getTestName());
            log.debug("Test details: {}", test);
            log.info("========================================");

            return ResponseEntity.ok(test);

        } catch (Exception e) {
            log.error("========================================");
            log.error("API ERROR: Test not found");
            log.error("========================================");
            log.error("Test ID: {}", id);
            log.error("Error: {}", e.getMessage(), e);
            log.error("========================================");

            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Start test page
     */
    @GetMapping("/test/{id}/start")
    public String startTest(
            @PathVariable Long id,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("========================================");
        log.info("Starting Test");
        log.info("========================================");
        log.info("Test ID: {}", id);
        log.info("User: {}", userDetails != null ? userDetails.getUsername() : "Anonymous");
        log.info("Timestamp: {}", java.time.LocalDateTime.now());

        try {
            // Fetch test details
            CreateTestDTO test = createTestService.getTestById(id);

            log.info("Test loaded: {}", test.getTestName());
            log.info("Subject: {}", test.getSubjectName());
            log.info("Total Questions: {}", test.getTotalQuestions());
            log.info("Duration: {} minutes", test.getDuration());
            log.info("Passing %: {}", test.getPassingPercentage());
            log.info("Tab Switch Limit: {}", test.getTabSwitchLimit());

            model.addAttribute("test", test);

            // Fetch questions for this test
            // TODO: Implement question fetching logic
            // List<QuestionDTO> questions = questionService.getRandomQuestions(
            //     test.getSubjectName(),
            //     test.getTotalQuestions()
            // );
            // model.addAttribute("questions", questions);

            log.info("Test start page loaded successfully");
            log.info("========================================");

            return "test-page"; // Create this template

        } catch (Exception e) {
            log.error("========================================");
            log.error("ERROR: Failed to start test");
            log.error("========================================");
            log.error("Test ID: {}", id);
            log.error("User: {}", userDetails != null ? userDetails.getUsername() : "Anonymous");
            log.error("Error: {}", e.getMessage(), e);
            log.error("========================================");

            model.addAttribute("error", "Test not found or unavailable");
            return "redirect:/student/dashboard";
        }
    }

    /**
     * API: Get test statistics
     */
    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<?> getStudentStats(
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("========================================");
        log.info("API: Fetch Student Statistics");
        log.info("========================================");
        log.info("User: {}", userDetails != null ? userDetails.getUsername() : "Anonymous");

        try {
            // TODO: Implement statistics logic
            var stats = new Object() {
                public final int completedTests = 0;
                public final double avgScore = 0.0;
                public final int totalHours = 0;
                public final String lastTestDate = "N/A";
            };

            log.info("Stats retrieved successfully");
            log.info("========================================");

            return ResponseEntity.ok(stats);

        } catch (Exception e) {
            log.error("API ERROR: Failed to fetch stats - {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}