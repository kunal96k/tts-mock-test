package com.tts.testApp.controller;

import com.tts.testApp.exception.DuplicateResourceException;
import com.tts.testApp.exception.ResourceNotFoundException;
import com.tts.testApp.model.Student;
import com.tts.testApp.service.Studentservice;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {


}