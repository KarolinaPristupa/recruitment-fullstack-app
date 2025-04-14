package com.example.server.Service;

import com.example.server.Models.Invite;
import com.example.server.Models.Notification;
import com.example.server.Models.Response;
import com.example.server.Models.User;
import com.example.server.Models.Vacancy;
import com.example.server.Repository.InviteRepository;
import com.example.server.Repository.NotificationRepository;
import com.example.server.Repository.ResponseRepository;
import com.example.server.Repository.UserRepository;
import com.example.server.Repository.VacancyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final InviteRepository inviteRepository;
    private final ResponseRepository responseRepository;
    private final VacancyRepository vacancyRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            InviteRepository inviteRepository,
            ResponseRepository responseRepository,
            VacancyRepository vacancyRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.inviteRepository = inviteRepository;
        this.responseRepository = responseRepository;
        this.vacancyRepository = vacancyRepository;
    }

    public Notification save(Notification notification) {
        logger.info("Сохранение уведомления: notificationId={}", notification.getNotificationId());
        return notificationRepository.save(notification);
    }

    public Notification createNotification(String message, String details, User sender, User recipient) {
        logger.info("Создание уведомления от senderId={} для recipientId={}", sender.getUsersId(), recipient.getUsersId());
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setDetails(details);
        notification.setSender(sender);
        notification.setRecipient(recipient);
        notification.setResponse(null);
        return notificationRepository.save(notification);
    }

    public List<Notification> getChat(Integer user1, Integer user2) {
        logger.info("Получение чата между user1={} и user2={}", user1, user2);
        List<Notification> notifications = notificationRepository.findChatBetween(user1, user2);
        logger.info("Найдено {} сообщений", notifications.size());
        return notifications;
    }

    public List<User> getChatRecipients(Integer userId) {
        logger.info("Получение чатов для userId={}", userId);
        List<Notification> notifs = notificationRepository.findBySender_UsersIdOrRecipient_UsersId(userId, userId);
        logger.info("Найдено {} уведомлений для userId={}", notifs.size(), userId);

        List<User> recipients = notifs.stream()
                .flatMap(n -> Stream.of(n.getSender(), n.getRecipient()))
                .filter(u -> u != null && !u.getUsersId().equals(userId))
                .distinct()
                .collect(Collectors.toList());

        logger.info("Найдено {} уникальных получателей", recipients.size());
        return recipients;
    }

    public Optional<Notification> findById(Integer notificationId) {
        logger.info("Поиск уведомления: notificationId={}", notificationId);
        return notificationRepository.findById(notificationId);
    }

    public void editNotification(Integer notificationId, Integer userId, String details) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    logger.error("Уведомление не найдено: notificationId={}", notificationId);
                    return new RuntimeException("Уведомление не найдено");
                });

        if (!notification.getSender().getUsersId().equals(userId)) {
            logger.warn("Попытка редактирования чужого уведомления: notificationId={}, userId={}", notificationId, userId);
            throw new RuntimeException("Нет прав для редактирования");
        }

        notification.setDetails(details);
        notificationRepository.save(notification);
        logger.info("Уведомление отредактировано: notificationId={}", notificationId);
    }

    public void editInviteNotification(Integer notificationId, Integer userId, LocalDateTime date) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    logger.error("Уведомление не найдено: notificationId={}", notificationId);
                    return new RuntimeException("Уведомление не найдено");
                });

        if (!notification.getSender().getUsersId().equals(userId)) {
            logger.warn("Попытка редактирования чужого уведомления: notificationId={}, userId={}", notificationId, userId);
            throw new RuntimeException("Нет прав для редактирования");
        }

        Optional<Invite> inviteOpt = inviteRepository.findByNotification_NotificationId(notificationId);
        if (inviteOpt.isEmpty()) {
            logger.error("Приглашение не найдено для уведомления: notificationId={}", notificationId);
            throw new RuntimeException("Приглашение не найдено");
        }

        Invite invite = inviteOpt.get();
        invite.setDate(date);
        inviteRepository.save(invite);
        notification.setDetails(date.toString());
        notificationRepository.save(notification);
        logger.info("Приглашение отредактировано: notificationId={}, date={}", notificationId, date);
    }

    public void editResponseNotification(Integer notificationId, Integer userId, String vacancyName) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    logger.error("Уведомление не найдено: notificationId={}", notificationId);
                    return new RuntimeException("Уведомление не найдено");
                });

        if (!notification.getSender().getUsersId().equals(userId)) {
            logger.warn("Попытка редактирования чужого уведомления: notificationId={}, userId={}", notificationId, userId);
            throw new RuntimeException("Нет прав для редактирования");
        }

        Response response = notification.getResponseEntity();
        if (response == null) {
            logger.error("Отклик не найден для уведомления: notificationId={}", notificationId);
            throw new RuntimeException("Отклик не найден");
        }

        Vacancy vacancy = vacancyRepository.findByPosition(vacancyName)
                .orElseThrow(() -> {
                    logger.error("Вакансия не найдена: vacancyName={}", vacancyName);
                    return new RuntimeException("Вакансия не найдена");
                });

        response.setVacancy(vacancy);
        responseRepository.save(response);
        notification.setDetails(vacancyName);
        notificationRepository.save(notification);
        logger.info("Отклик отредактирован: notificationId={}, vacancyName={}", notificationId, vacancyName);
    }

    public boolean vacancyExists(String vacancyName) {
        return vacancyRepository.findByPosition(vacancyName).isPresent();
    }

    @Transactional
    public void deleteNotification(Integer notificationId, Integer userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Уведомление не найдено"));

        if (!notification.getSender().getUsersId().equals(userId)) {
            throw new RuntimeException("Нет прав для удаления уведомления");
        }

        // Удаляем INVITE, если есть
        inviteRepository.findByNotification_NotificationId(notificationId)
                .ifPresent(inviteRepository::delete);

        // Удаляем RESPONSE, если есть
        Response response = notification.getResponseEntity();
        if (response != null) {
            // Обнуляем ссылку на notification
            response.setNotification(null);
            responseRepository.save(response); // сохранить без связи
            responseRepository.delete(response); // потом удалить
        }

        // Удаляем сам notification
        notificationRepository.delete(notification);
    }

    public void updateNotificationResponse(Integer notificationId, Integer userId, String response) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    logger.error("Уведомление не найдено: notificationId={}", notificationId);
                    return new RuntimeException("Уведомление не найдено");
                });

        if (!notification.getRecipient().getUsersId().equals(userId)) {
            logger.warn("Попытка ответа на чужое уведомление: notificationId={}, userId={}", notificationId, userId);
            throw new RuntimeException("Нет прав для ответа");
        }

        notification.setResponse(response);
        notificationRepository.save(notification);
        logger.info("Ответ обновлен: notificationId={}, response={}", notificationId, response);
    }
}