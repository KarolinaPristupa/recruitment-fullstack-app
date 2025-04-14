package com.example.server.controllers;

import com.example.server.JWT.JwtUtil;
import com.example.server.Models.Candidate;
import com.example.server.Models.Invite;
import com.example.server.Models.User;
import com.example.server.Models.Vacancy;
import com.example.server.Service.CandidateService;
import com.example.server.Service.InviteService;
import com.example.server.Service.UserService;
import com.example.server.Service.VacancyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/invites")
public class InviteController {

    private static final Logger logger = LoggerFactory.getLogger(InviteController.class);

    private final InviteService inviteService;
    private final CandidateService candidateService;
    private final UserService userService;
    private final VacancyService vacancyService;
    private final JwtUtil jwtUtil;

    public InviteController(InviteService inviteService,
                            CandidateService candidateService,
                            UserService userService,
                            VacancyService vacancyService,
                            JwtUtil jwtUtil) {
        this.inviteService = inviteService;
        this.candidateService = candidateService;
        this.userService = userService;
        this.vacancyService = vacancyService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping
    public ResponseEntity<?> createInvite(@RequestHeader("Authorization") String authHeader,
                                          @RequestParam Integer candidateId,
                                          @RequestParam Integer vacancyId,
                                          @RequestParam String interviewDate) {
        try {
            logger.info("Получен запрос на создание приглашения: candidateId={}, vacancyId={}, interviewDate={}",
                    candidateId, vacancyId, interviewDate);

            // Извлечение и проверка токена
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

            // Получение sender (авторизованного пользователя)
            User sender = userService.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.error("Пользователь не найден: {}", email);
                        return new RuntimeException("Пользователь не найден");
                    });

            // Получение candidate
            Candidate candidate = candidateService.findById(candidateId)
                    .orElseThrow(() -> new RuntimeException("Кандидат не найден"));

            User recipient = candidate.getUser();


            // Получение vacancy
            Vacancy vacancy = vacancyService.findById(vacancyId);

            // Парсинг даты
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDateTime dateTime = LocalDateTime.parse(interviewDate, formatter);
            logger.debug("Спарсена дата: {}", dateTime);

            // Создание приглашения
            Invite invite = inviteService.createInvite(sender, recipient, vacancy, dateTime);
            logger.info("Приглашение успешно создано: inviteId={}", invite.getInviteId());

            return ResponseEntity.ok(invite);

        } catch (Exception e) {
            logger.error("Ошибка при создании приглашения: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Ошибка при создании приглашения: " + e.getMessage());
        }
    }
}