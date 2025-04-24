package com.example.server.controllers;

import com.example.server.JWT.JwtUtil;
import com.example.server.Service.InterviewStatisticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/statistics")
public class InterviewStatisticsController {

    private final InterviewStatisticsService statisticsService;
    private final JwtUtil jwtUtil;
    private static final Logger logger = LoggerFactory.getLogger(InterviewStatisticsController.class);

    public InterviewStatisticsController(InterviewStatisticsService statisticsService, JwtUtil jwtUtil) {
        this.statisticsService = statisticsService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/interview-report")
    public ResponseEntity<byte[]> generateInterviewReport(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid or missing token: {}", authHeader);
                return ResponseEntity.status(401).body(null);
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);

            if (!jwtUtil.isTokenValid(token, email)) {
                logger.warn("Invalid or expired token for email: {}", email);
                return ResponseEntity.status(401).body(null);
            }

            byte[] reportBytes = statisticsService.generateInterviewStatisticsReport();
            String filename = "Interview_Statistics_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(reportBytes);
        } catch (IOException e) {
            logger.error("Error generating interview report: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(null);
        }
    }
}