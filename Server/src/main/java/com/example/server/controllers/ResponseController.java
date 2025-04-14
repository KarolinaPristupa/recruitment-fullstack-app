package com.example.server.controllers;

import com.example.server.JWT.JwtUtil;
import com.example.server.Models.Response;
import com.example.server.Service.ResponseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/responses")
public class ResponseController {

    private static final Logger logger = LoggerFactory.getLogger(ResponseController.class);

    private final ResponseService responseService;
    private final JwtUtil jwtUtil;

    public ResponseController(ResponseService responseService, JwtUtil jwtUtil) {
        this.responseService = responseService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<?> createResponse(@RequestHeader("Authorization") String authHeader,
                                            @RequestBody ResponseRequest request) {
        try {
            logger.info("Получен запрос на создание отклика: vacancyId={}", request.getVacancyId());

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Токен отсутствует или неверный формат: {}", authHeader);
                return ResponseEntity.status(401).body("Токен отсутствует или неверный формат");
            }

            String token = authHeader.substring(7);
            logger.debug("Извлечен токен: {}", token);

            String email = jwtUtil.extractUsername(token);
            logger.debug("Извлечен email из токена: {}", email);

            if (!jwtUtil.isTokenValid(token, email)) {
                logger.warn("Недействительный или просроченный токен для email: {}", email);
                return ResponseEntity.status(401).body("Недействительный или просроченный токен");
            }

            Response response = responseService.createResponse(email, request.getVacancyId());
            logger.info("Отклик успешно создан: responseId={}", response.getResponseId());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при создании отклика: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body("Ошибка: " + e.getMessage());
        }
    }

    // Вспомогательный класс для запроса
    public static class ResponseRequest {
        private Integer vacancyId;

        public Integer getVacancyId() {
            return vacancyId;
        }

        public void setVacancyId(Integer vacancyId) {
            this.vacancyId = vacancyId;
        }
    }
}