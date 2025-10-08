package com.tts.testApp.config;

import com.tts.testApp.model.Admin;
import com.tts.testApp.model.Student;
import com.tts.testApp.repository.AdminRepository;
import com.tts.testApp.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final StudentRepository studentRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Attempting to load user: {}", username);

        // Try ADMIN first
        Admin admin = adminRepository.findByUsername(username).orElse(null);
        if (admin != null) {
            log.info("Admin found: {} with role: {}", username, admin.getRole());

            if (!admin.getIsActive()) {
                log.warn("Admin account is deactivated: {}", username);
                throw new UsernameNotFoundException("Account is deactivated");
            }

            if (admin.getAccountLockedUntil() != null &&
                    LocalDateTime.now().isBefore(admin.getAccountLockedUntil())) {
                log.warn("Admin account locked: {}", username);
                throw new UsernameNotFoundException("Account is locked");
            }

            return User.builder()
                    .username(admin.getUsername())
                    .password(admin.getPassword())
                    .authorities(Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + admin.getRole())
                    ))
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(!admin.getIsActive())
                    .build();
        }

        // Try STUDENT next
        Student student = studentRepository.findByEmail(username).orElse(null);
        if (student != null) {
            log.info("Student found: {} with role: {}", username, student.getRole());
            log.info("Student email: {}, password: {}", student.getEmail(), student.getPassword());

            return User.builder()
                    .username(student.getEmail())
                    .password(student.getPassword())
                    .authorities(Collections.singletonList(
                            new SimpleGrantedAuthority(student.getRole())
                    ))
                    .accountLocked(!student.isAccountNonLocked())
                    .disabled(!student.isEnabled())
                    .build();
        }

        // If neither found
        log.error("User not found: {}", username);
        throw new UsernameNotFoundException("User not found: " + username);
    }
}
