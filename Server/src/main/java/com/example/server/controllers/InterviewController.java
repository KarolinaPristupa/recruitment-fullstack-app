package com.example.server.controllers;

import com.example.server.JWT.JwtUtil;
import com.example.server.Models.Interview;
import com.example.server.Service.InterviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/interviews")
public class InterviewController {
    private static final Logger logger = LoggerFactory.getLogger(InterviewController.class);

    private final InterviewService interviewService;
    private final JwtUtil jwtUtil;

    public InterviewController(InterviewService interviewService, JwtUtil jwtUtil) {
        this.interviewService = interviewService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping
    public ResponseEntity<?> getInterviews(@RequestHeader("Authorization") String authHeader) {
        try {
            logger.info("Received request to fetch interviews");

            // Validate and extract token
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Token is missing or invalid format: {}", authHeader);
                return ResponseEntity.status(401).body("Токен отсутствует или неверный формат");
            }

            String token = authHeader.substring(7);
            logger.debug("Extracted token: {}", token);

            String email = jwtUtil.extractUsername(token);
            logger.debug("Extracted email from token: {}", email);

            if (!jwtUtil.isTokenValid(token, email)) {
                logger.warn("Invalid or expired token for email: {}", email);
                return ResponseEntity.status(401).body("Недействительный или просроченный токен");
            }

            // Fetch interviews
            List<Interview> interviews = interviewService.getInterviewsForUser(email);
            logger.info("Successfully fetched {} interviews for user: {}", interviews.size(), email);
            return ResponseEntity.ok(interviews);

        } catch (Exception e) {
            logger.error("Error fetching interviews: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("Ошибка при получении собеседований: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteInterview(
            @PathVariable("id") Integer interviewId,
            @RequestHeader("Authorization") String authHeader
    ) {
        try {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractClaim(token, claims -> claims.get("role", String.class));
            if (!jwtUtil.isTokenValid(token, email)) {
                return ResponseEntity.status(401).body("Недействительный или просроченный токен");
            }
            interviewService.deleteInterview(interviewId, email, role);
            return ResponseEntity.ok("Собеседование успешно удалено");
        } catch (Exception e) {
            logger.error("Error deleting interview: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body("Ошибка при удалении собеседования: " + e.getMessage());
        }
    }
}