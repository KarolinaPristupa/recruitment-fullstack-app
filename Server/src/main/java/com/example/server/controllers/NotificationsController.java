package com.example.server.controllers;

import com.example.server.JWT.JwtUtil;
import com.example.server.Models.Invite;
import com.example.server.Models.Notification;
import com.example.server.Models.User;
import com.example.server.Service.InviteService;
import com.example.server.Service.NotificationService;
import com.example.server.Service.ResponseService;
import com.example.server.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationsController {
    private final NotificationService notificationService;
    private final InviteService inviteService;
    private final ResponseService responseService;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(NotificationsController.class);

    @GetMapping("/chats")
    public ResponseEntity<?> getChats(@RequestHeader("Authorization") String token) {
        try {
            logger.info("Получен запрос на получение чатов, токен: {}", token);
            if (token == null || !token.startsWith("Bearer ")) {
                logger.warn("Некорректный формат токена");
                return ResponseEntity.status(401).body(Map.of("success", false, "error", "Некорректный формат токена"));
            }

            String email = jwtUtil.extractUsername(token.substring(7));
            logger.debug("Извлечен email: {}", email);

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.error("Пользователь не найден: {}", email);
                        return new RuntimeException("Пользователь не найден");
                    });
            logger.info("Найден пользователь: usersId={}", user.getUsersId());

            List<User> chats = notificationService.getChatRecipients(user.getUsersId());
            logger.info("Найдено {} чатов для usersId={}", chats.size(), user.getUsersId());

            List<Map<String, Object>> response = chats.stream().map(u -> {
                Map<String, Object> map = new HashMap<>();
                map.put("recipientId", u.getUsersId());
                map.put("recipientName", u.getFirstName() + " " + u.getLastName());
                return map;
            }).collect(Collectors.toList());

            logger.debug("Ответ: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении чатов: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", "Ошибка сервера: " + e.getMessage()));
        }
    }

    @GetMapping("/chat/{withUserId}")
    public ResponseEntity<?> getChat(@RequestHeader("Authorization") String token,
                                     @PathVariable Integer withUserId) {
        try {
            String email = jwtUtil.extractUsername(token.substring(7));
            User user = userService.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.error("Пользователь не найден: {}", email);
                        return new RuntimeException("Пользователь не найден");
                    });

            List<Notification> messages = notificationService.getChat(user.getUsersId(), withUserId);

            List<Map<String, Object>> response = messages.stream().map(n -> {
                Map<String, Object> map = new HashMap<>();
                map.put("messageId", n.getNotificationId());
                map.put("message", n.getMessage());
                map.put("details", n.getDetails());
                map.put("sentByMe", n.getSender().getUsersId().equals(user.getUsersId()));
                map.put("response", n.getResponse());
                Optional<Invite> invite = inviteService.findByNotificationId(n.getNotificationId());
                if (invite.isPresent()) {
                    map.put("date", invite.get().getDate().toString());
                    map.put("type", "invite");
                } else {
                    Optional<com.example.server.Models.Response> responseOpt = responseService.findByNotificationId(n.getNotificationId());
                    if (responseOpt.isPresent()) {
                        map.put("vacancyName", responseOpt.get().getVacancy().getPosition());
                        map.put("type", "response");
                    } else {
                        map.put("type", "message");
                    }
                }
                return map;
            }).collect(Collectors.toList());

            logger.info("Возвращено {} сообщений для чата с userId={}", response.size(), withUserId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Ошибка при получении чата: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", "Ошибка сервера: " + e.getMessage()));
        }
    }

    @PutMapping("/{notificationId}")
    public ResponseEntity<?> editNotification(@RequestHeader("Authorization") String token,
                                              @PathVariable Integer notificationId,
                                              @RequestBody Map<String, String> data) {
        try {
            logger.info("Получен запрос на редактирование уведомления: notificationId={}", notificationId);

            if (token == null || !token.startsWith("Bearer ")) {
                logger.warn("Некорректный формат токена");
                return ResponseEntity.status(401).body(Map.of("success", false, "error", "Некорректный формат токена"));
            }

            String email = jwtUtil.extractUsername(token.substring(7));
            logger.debug("Извлечен email: {}", email);

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.error("Пользователь не найден: {}", email);
                        return new RuntimeException("Пользователь не найден");
                    });

            String details = data.get("details");
            String dateStr = data.get("date");
            String vacancyName = data.get("vacancyName");

            if (details == null && dateStr == null && vacancyName == null) {
                logger.warn("Отсутствуют данные для редактирования: notificationId={}", notificationId);
                return ResponseEntity.status(400).body(Map.of("success", false, "error", "Не указаны данные для редактирования"));
            }

            Notification notification = notificationService.findById(notificationId)
                    .orElseThrow(() -> {
                        logger.error("Уведомление не найдено: notificationId={}", notificationId);
                        return new RuntimeException("Уведомление не найдено");
                    });

            if (dateStr != null) {
                try {
                    LocalDateTime date = LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    if (date.isBefore(LocalDateTime.now())) {
                        logger.warn("Дата в прошлом: date={}", dateStr);
                        return ResponseEntity.status(400).body(Map.of("success", false, "error", "Дата не может быть в прошлом"));
                    }
                    notificationService.editInviteNotification(notificationId, user.getUsersId(), date);
                } catch (DateTimeParseException e) {
                    logger.warn("Некорректный формат даты: date={}", dateStr);
                    return ResponseEntity.status(400).body(Map.of("success", false, "error", "Некорректный формат даты"));
                }
            } else if (vacancyName != null) {
                boolean vacancyExists = notificationService.vacancyExists(vacancyName);
                if (!vacancyExists) {
                    logger.warn("Вакансия не найдена: vacancyName={}", vacancyName);
                    return ResponseEntity.status(400).body(Map.of("success", false, "error", "Вакансия с таким именем не существует"));
                }
                notificationService.editResponseNotification(notificationId, user.getUsersId(), vacancyName);
            } else {
                if (details == null) {
                    logger.warn("Детали не указаны для обычного уведомления: notificationId={}", notificationId);
                    return ResponseEntity.status(400).body(Map.of("success", false, "error", "Детали обязательны для обычного уведомления"));
                }
                notificationService.editNotification(notificationId, user.getUsersId(), details);
            }

            logger.info("Уведомление успешно отредактировано: notificationId={}", notificationId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Ошибка при редактировании уведомления: notificationId={}, error={}", notificationId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", "Ошибка сервера: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<?> deleteNotification(@RequestHeader("Authorization") String token,
                                                @PathVariable Integer notificationId) {
        try {
            logger.info("Получен запрос на удаление уведомления: notificationId={}", notificationId);

            if (token == null || !token.startsWith("Bearer ")) {
                logger.warn("Некорректный формат токена");
                return ResponseEntity.status(401).body(Map.of("success", false, "error", "Некорректный формат токена"));
            }

            String email = jwtUtil.extractUsername(token.substring(7));
            logger.debug("Извлечен email: {}", email);

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.error("Пользователь не найден: {}", email);
                        return new RuntimeException("Пользователь не найден");
                    });

            logger.debug("Проверка уведомления: notificationId={}", notificationId);
            Notification notification = notificationService.findById(notificationId)
                    .orElseThrow(() -> {
                        logger.error("Уведомление не найдено: notificationId={}", notificationId);
                        return new RuntimeException("Уведомление не найдено");
                    });

            logger.debug("Удаление уведомления для userId={}", user.getUsersId());
            notificationService.deleteNotification(notificationId, user.getUsersId().intValue());

            logger.info("Уведомление успешно удалено: notificationId={}", notificationId);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Ошибка при удалении уведомления: notificationId={}, error={}", notificationId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", "Ошибка сервера: " + e.getMessage()));
        }
    }

    @PutMapping("/{notificationId}/response")
    public ResponseEntity<?> updateNotificationResponse(@RequestHeader("Authorization") String token,
                                                        @PathVariable Integer notificationId,
                                                        @RequestBody Map<String, String> data) {
        try {
            logger.info("Получен запрос на обновление ответа: notificationId={}", notificationId);

            if (token == null || !token.startsWith("Bearer ")) {
                logger.warn("Некорректный формат токена");
                return ResponseEntity.status(401).body(Map.of("success", false, "error", "Некорректный формат токена"));
            }

            String email = jwtUtil.extractUsername(token.substring(7));
            logger.debug("Извлечен email: {}", email);

            User user = userService.findByEmail(email)
                    .orElseThrow(() -> {
                        logger.error("Пользователь не найден: {}", email);
                        return new RuntimeException("Пользователь не найден");
                    });

            String response = data.get("response");
            String interviewDateStr = data.get("interviewDate");
            LocalDateTime interviewDate = null;

            if (response == null) {
                logger.warn("Ответ не указан: notificationId={}", notificationId);
                return ResponseEntity.status(400).body(Map.of("success", false, "error", "Ответ обязателен"));
            }

            if (interviewDateStr != null) {
                try {
                    interviewDate = LocalDateTime.parse(interviewDateStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    if (interviewDate.isBefore(LocalDateTime.now())) {
                        logger.warn("Дата собеседования в прошлом: date={}", interviewDateStr);
                        return ResponseEntity.status(400).body(Map.of("success", false, "error", "Дата собеседования не может быть в прошлом"));
                    }
                } catch (DateTimeParseException e) {
                    logger.warn("Некорректный формат даты: date={}", interviewDateStr);
                    return ResponseEntity.status(400).body(Map.of("success", false, "error", "Некорректный формат даты"));
                }
            }

            notificationService.updateNotificationResponse(notificationId, user.getUsersId(), response, interviewDate);

            logger.info("Ответ успешно обновлен: notificationId={}, response={}", notificationId, response);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (Exception e) {
            logger.error("Ошибка при обновлении ответа: notificationId={}, error={}", notificationId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("success", false, "error", "Ошибка сервера: " + e.getMessage()));
        }
    }
}