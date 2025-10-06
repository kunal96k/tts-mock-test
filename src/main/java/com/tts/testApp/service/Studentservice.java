package com.tts.testApp.service;

import com.tts.testApp.model.Student;
import com.tts.testApp.exception.DuplicateResourceException;
import com.tts.testApp.exception.ResourceNotFoundException;
import com.tts.testApp.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class Studentservice {


}