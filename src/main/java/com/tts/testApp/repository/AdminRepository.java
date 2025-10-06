package com.tts.testApp.repository;

import com.tts.testApp.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);

    Optional<Admin> findByEmail(String email);

    Optional<Admin> findByUsernameAndIsActiveTrue(String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE Admin a SET a.lastLoginAt = :loginTime, a.lastLoginIp = :ipAddress WHERE a.username = :username")
    void updateLastLogin(@Param("username") String username,
                         @Param("loginTime") LocalDateTime loginTime,
                         @Param("ipAddress") String ipAddress);

    @Modifying
    @Query("UPDATE Admin a SET a.loginAttempts = :attempts WHERE a.username = :username")
    void updateLoginAttempts(@Param("username") String username, @Param("attempts") Integer attempts);

    @Modifying
    @Query("UPDATE Admin a SET a.accountLockedUntil = :lockUntil WHERE a.username = :username")
    void lockAccount(@Param("username") String username, @Param("lockUntil") LocalDateTime lockUntil);
}