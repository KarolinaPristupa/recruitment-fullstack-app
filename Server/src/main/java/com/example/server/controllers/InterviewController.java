package com.example.server.controllers;

import com.example.server.JWT.JwtUtil;
import com.example.server.Models.Interview;
import com.example.server.Service.InterviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

    @PutMapping("/{id}")
    public ResponseEntity<?> updateInterview(
            @PathVariable("id") Integer interviewId,
            @RequestHeader("Authorization") String authHeader,
            @RequestBody UpdateInterviewRequest request
    ) {
        try {
            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);
            String role = jwtUtil.extractClaim(token, claims -> claims.get("role", String.class));
            if (!jwtUtil.isTokenValid(token, email)) {
                return ResponseEntity.status(401).body("Недействительный или просроченный токен");
            }
            if (request.getDate() == null && (request.getPosition() == null || request.getPosition().trim().isEmpty())) {
                return ResponseEntity.status(400).body("Не указаны дата или позиция для обновления");
            }
            Interview updatedInterview = interviewService.updateInterview(
                    interviewId,
                    email,
                    role,
                    request.getDate(),
                    request.getPosition()
            );
            return ResponseEntity.ok(updatedInterview);
        } catch (Exception e) {
            logger.error("Error updating interview: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body("Ошибка при обновлении собеседования: " + e.getMessage());
        }
    }

    public static class UpdateInterviewRequest {
        private LocalDateTime date;
        private String position;

        public LocalDateTime getDate() {
            return date;
        }

        public void setDate(LocalDateTime date) {
            this.date = date;
        }

        public String getPosition() {
            return position;
        }

        public void setPosition(String position) {
            this.position = position;
        }
    }
}