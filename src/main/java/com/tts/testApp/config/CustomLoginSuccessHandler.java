package com.tts.testApp.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        // Get the role parameter from login form
        String requestedRole = request.getParameter("role");

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean isStudent = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_STUDENT"));

        // Validate that the user's actual role matches the requested role
        if (requestedRole != null && requestedRole.equals("ADMIN") && !isAdmin) {
            // User tried to login as admin but doesn't have admin role
            response.sendRedirect("/login?role=ADMIN&error=unauthorized");
            return;
        }

        if (requestedRole != null && requestedRole.equals("STUDENT") && !isStudent) {
            // User tried to login as student but doesn't have student role
            response.sendRedirect("/login?role=STUDENT&error=unauthorized");
            return;
        }

        // If role matches, redirect to appropriate dashboard
        if (isAdmin) {
            response.sendRedirect("/admin-dashboard");
        } else if (isStudent) {
            response.sendRedirect("/student-dashboard");
        } else {
            response.sendRedirect("/login?error=true");
        }
    }
}