package com.tts.testApp.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import com.tts.testApp.dto.CSVQuestionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CSVParserService {

    /**
     * Parse CSV file and extract questions
     * Expected CSV format:
     * Question,Option A,Option B,Option C,Option D,Correct Answer,Explanation,Marks,Difficulty
     */
    public List<CSVQuestionDTO> parseCSV(MultipartFile file) throws IOException {
        List<CSVQuestionDTO> questions = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVReader csvReader = new CSVReader(reader)) {

            List<String[]> rows = csvReader.readAll();

            if (rows.isEmpty()) {
                throw new IOException("CSV file is empty");
            }

            // Skip header row
            boolean firstRow = true;
            int rowNumber = 0;

            for (String[] row : rows) {
                rowNumber++;

                if (firstRow) {
                    firstRow = false;
                    validateHeader(row);
                    continue;
                }

                if (row.length < 6) {
                    log.warn("Row {} has insufficient columns: {}", rowNumber, row.length);
                    continue;
                }

                try {
                    CSVQuestionDTO question = parseRow(row, rowNumber);
                    if (question != null && question.isValid()) {
                        question.normalize();
                        questions.add(question);
                    } else if (question != null) {
                        log.warn("Invalid question at row {}: {}", rowNumber,
                                question.getValidationErrors());
                    }
                } catch (Exception e) {
                    log.error("Error parsing row {}: {}", rowNumber, e.getMessage());
                }
            }

            if (questions.isEmpty()) {
                throw new IOException("No valid questions found in CSV file");
            }

            log.info("Successfully parsed {} questions from CSV", questions.size());
            return questions;

        } catch (CsvException e) {
            log.error("CSV parsing error: {}", e.getMessage());
            throw new IOException("Error parsing CSV file: " + e.getMessage(), e);
        }
    }

    private void validateHeader(String[] header) throws IOException {
        if (header.length < 6) {
            throw new IOException("CSV header must have at least 6 columns: " +
                    "Question, Option A, Option B, Option C, Option D, Correct Answer");
        }
    }

    private CSVQuestionDTO parseRow(String[] row, int rowNumber) {
        CSVQuestionDTO question = new CSVQuestionDTO();

        try {
            question.setQuestionText(getColumnValue(row, 0));
            question.setOptionA(getColumnValue(row, 1));
            question.setOptionB(getColumnValue(row, 2));
            question.setOptionC(getColumnValue(row, 3));
            question.setOptionD(getColumnValue(row, 4));
            question.setCorrectAnswer(getColumnValue(row, 5));

            // Optional columns
            question.setExplanation(getColumnValue(row, 6));

            // Marks (default 1)
            try {
                String marksStr = getColumnValue(row, 7);
                question.setMarks(marksStr != null && !marksStr.isEmpty() ?
                        Integer.parseInt(marksStr.trim()) : 1);
            } catch (NumberFormatException e) {
                question.setMarks(1);
            }

            // Difficulty (default MEDIUM)
            String difficulty = getColumnValue(row, 8);
            question.setDifficultyLevel(difficulty != null && !difficulty.isEmpty() ?
                    difficulty : "MEDIUM");

            return question;

        } catch (Exception e) {
            log.error("Error parsing row {}: {}", rowNumber, e.getMessage());
            return null;
        }
    }

    private String getColumnValue(String[] row, int index) {
        if (index < row.length && row[index] != null) {
            return row[index].trim();
        }
        return null;
    }

    /**
     * Validate CSV file before processing
     */
    public void validateCSVFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("File is empty");
        }

        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".csv")) {
            throw new IOException("Only CSV files are allowed");
        }

        // Check file size (5MB max)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IOException("File size exceeds 5MB limit");
        }

        // Check if file has content
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine();
            if (firstLine == null || firstLine.trim().isEmpty()) {
                throw new IOException("CSV file is empty");
            }
        }
    }
}