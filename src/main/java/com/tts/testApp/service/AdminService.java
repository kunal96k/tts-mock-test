package com.tts.testApp.service;

import com.tts.testApp.dto.SignUpDTO;
import com.tts.testApp.model.Admin;
import com.tts.testApp.exception.AdminAlreadyExistsException;
import com.tts.testApp.exception.PasswordMismatchException;
import com.tts.testApp.repository.AdminRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Admin registerAdmin(SignUpDTO signUpDTO, HttpServletRequest request) {
        log.info("Attempting to register admin with username: {}", signUpDTO.getUsername());

        // Validate password confirmation
        if (!signUpDTO.getPassword().equals(signUpDTO.getConfirmPassword())) {
            throw new PasswordMismatchException("Passwords do not match");
        }

        // Check if username already exists
        if (adminRepository.existsByUsername(signUpDTO.getUsername())) {
            throw new AdminAlreadyExistsException("Username already exists: " + signUpDTO.getUsername());
        }

        // Check if email already exists
        if (adminRepository.existsByEmail(signUpDTO.getEmail())) {
            throw new AdminAlreadyExistsException("Email already exists: " + signUpDTO.getEmail());
        }

        // Create new admin
        Admin admin = new Admin();
        admin.setFullName(signUpDTO.getFullName().trim());
        admin.setUsername(signUpDTO.getUsername().toLowerCase().trim());
        admin.setEmail(signUpDTO.getEmail().toLowerCase().trim());
        admin.setPassword(passwordEncoder.encode(signUpDTO.getPassword()));
        admin.setRole("ADMIN");
        admin.setIsActive(true);
        admin.setRegistrationIp(getClientIp(request));
        admin.setLoginAttempts(0);

        Admin savedAdmin = adminRepository.save(admin);
        log.info("Admin registered successfully: {}", savedAdmin.getUsername());

        return savedAdmin;
    }

    public Optional<Admin> findByUsername(String username) {
        return adminRepository.findByUsername(username);
    }

    public Optional<Admin> findActiveAdminByUsername(String username) {
        return adminRepository.findByUsernameAndIsActiveTrue(username);
    }

    @Transactional
    public void updateLastLogin(String username, HttpServletRequest request) {
        String ipAddress = getClientIp(request);
        adminRepository.updateLastLogin(username, LocalDateTime.now(), ipAddress);
        adminRepository.updateLoginAttempts(username, 0);
        log.info("Updated last login for admin: {}", username);
    }

    @Transactional
    public void incrementLoginAttempts(String username) {
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            int attempts = admin.getLoginAttempts() + 1;
            adminRepository.updateLoginAttempts(username, attempts);

            // Lock account after 5 failed attempts for 15 minutes
            if (attempts >= 5) {
                LocalDateTime lockUntil = LocalDateTime.now().plusMinutes(15);
                adminRepository.lockAccount(username, lockUntil);
                log.warn("Account locked due to multiple failed login attempts: {}", username);
            }
        }
    }

    public boolean isAccountLocked(String username) {
        Optional<Admin> adminOpt = adminRepository.findByUsername(username);
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            if (admin.getAccountLockedUntil() != null) {
                if (LocalDateTime.now().isBefore(admin.getAccountLockedUntil())) {
                    return true;
                } else {
                    // Unlock account if lock time has passed
                    adminRepository.lockAccount(username, null);
                    adminRepository.updateLoginAttempts(username, 0);
                }
            }
        }
        return false;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}