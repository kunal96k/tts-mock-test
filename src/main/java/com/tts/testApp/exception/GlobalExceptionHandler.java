package com.tts.testApp.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // ========================================
    // FORM-BASED EXCEPTION HANDLERS (HTML)
    // These handle exceptions from form submissions
    // Returns: redirect to dashboard with flash messages
    // ========================================

    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(
            ResourceNotFoundException ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        // Only handle if it's a form request (not an API request)
        if (isApiRequest(request)) {
            throw ex; // Let REST handler handle it
        }

        redirectAttributes.addFlashAttribute("error", ex.getMessage());
        return "redirect:/admin/dashboard";
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public String handleDuplicateResourceException(
            DuplicateResourceException ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        // Only handle if it's a form request (not an API request)
        if (isApiRequest(request)) {
            throw ex; // Let REST handler handle it
        }

        // Determine which form triggered the error
        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/students")) {
            redirectAttributes.addFlashAttribute("studentError", ex.getMessage());
        } else {
            redirectAttributes.addFlashAttribute("subjectError", ex.getMessage());
        }

        return "redirect:/admin/dashboard";
    }

    @ExceptionHandler(InvalidInputException.class)
    public String handleInvalidInputException(
            InvalidInputException ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        // Only handle if it's a form request (not an API request)
        if (isApiRequest(request)) {
            throw ex; // Let REST handler handle it
        }

        redirectAttributes.addFlashAttribute("subjectError", ex.getMessage());
        return "redirect:/admin/dashboard";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String handleValidationExceptions(
            MethodArgumentNotValidException ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) throws MethodArgumentNotValidException {

        // Only handle if it's a form request (not an API request)
        if (isApiRequest(request)) {
            throw ex; // Let REST handler handle it
        }

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        // Get the first error message
        String firstError = errors.values().iterator().next();
        redirectAttributes.addFlashAttribute("subjectError", firstError);
        redirectAttributes.addFlashAttribute("validationErrors", errors);

        return "redirect:/admin/dashboard";
    }

    @ExceptionHandler(Exception.class)
    public String handleGlobalException(
            Exception ex,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        // Only handle if it's a form request (not an API request)
        if (isApiRequest(request)) {
            throw new RuntimeException(ex); // Let REST handler handle it
        }

        redirectAttributes.addFlashAttribute("error",
                "An unexpected error occurred: " + ex.getMessage());
        return "redirect:/admin/dashboard";
    }

    // ========================================
    // Helper Method: Detect if request is API call
    // ========================================
    private boolean isApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");

        // Check if URI contains "/api/"
        if (uri != null && uri.contains("/api/")) {
            return true;
        }

        // Check if client wants JSON response
        if (acceptHeader != null && acceptHeader.contains("application/json")) {
            return true;
        }

        return false;
    }
}