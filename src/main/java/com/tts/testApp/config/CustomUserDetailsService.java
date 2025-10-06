package com.tts.testApp.config;

import com.tts.testApp.model.Admin;
import com.tts.testApp.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);

        // Try to load from Admin table
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        // Check if account is active
        if (!admin.getIsActive()) {
            log.warn("Account is deactivated: {}", username);
            throw new UsernameNotFoundException("Account is deactivated");
        }

        // Check if account is locked
        if (admin.getAccountLockedUntil() != null &&
                LocalDateTime.now().isBefore(admin.getAccountLockedUntil())) {
            log.warn("Account is locked: {}", username);
            throw new UsernameNotFoundException("Account is locked due to multiple failed login attempts");
        }

        log.info("User loaded successfully: {}", username);

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
}