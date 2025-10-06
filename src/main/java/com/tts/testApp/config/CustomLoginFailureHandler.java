package com.tts.testApp.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomLoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {

        String role = request.getParameter("role");

        if ("ADMIN".equalsIgnoreCase(role)) {
            response.sendRedirect("/login?role=ADMIN&error=true");
        } else if ("STUDENT".equalsIgnoreCase(role)) {
            response.sendRedirect("/login?role=STUDENT&error=true");
        } else {
            response.sendRedirect("/login?error=true");
        }
    }
}

