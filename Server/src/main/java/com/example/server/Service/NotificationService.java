package com.example.server.Service;

import com.example.server.Models.*;
import com.example.server.Repository.*;
import com.example.server.Service.observer.NotificationPublisher;
import com.example.server.Service.observer.Observer;
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
public class NotificationService implements Observer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final InviteRepository inviteRepository;
    private final ResponseRepository responseRepository;
    private final VacancyRepository vacancyRepository;
    private final NotificationPublisher notificationPublisher;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            InviteRepository inviteRepository,
            ResponseRepository responseRepository,
            VacancyRepository vacancyRepository,
            NotificationPublisher notificationPublisher
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.inviteRepository = inviteRepository;
        this.responseRepository = responseRepository;
        this.vacancyRepository = vacancyRepository;
        this.notificationPublisher = notificationPublisher;
        this.notificationPublisher.subscribe(this); // Subscribe to events
    }

    public Notification save(Notification notification) {
        logger.info("Saving notification: notificationId={}", notification.getNotificationId());
        return notificationRepository.save(notification);
    }

    public Notification createNotification(String message, String details, User sender, User recipient) {
        logger.info("Creating notification from senderId={} for recipientId={}", sender.getUsersId(), recipient.getUsersId());
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setDetails(details);
        notification.setSender(sender);
        notification.setRecipient(recipient);
        notification.setResponse(null);
        return notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void update(String eventType, Object entity, User sender, User recipient) {
        String message = "";
        String details = "";

        // Skip notification creation for CREATE events, as it's handled in InviteService/ResponseService
        if ("CREATE".equals(eventType)) {
            return;
        }

        if (entity instanceof Invite) {
            Invite invite = (Invite) entity;
            Vacancy vacancy = invite.getVacancy();
            LocalDateTime date = invite.getDate();

            switch (eventType) {
                case "UPDATE":
                    message = String.format("Приглашение на вакансию %s обновлено", vacancy.getPosition());
                    details = String.format("Новая дата: %s, Время: %s", date.toLocalDate(), date.toLocalTime().withSecond(0).withNano(0));
                    break;
                case "DELETE":
                    message = String.format("Приглашение на вакансию %s отменено", vacancy.getPosition());
                    details = "Приглашение удалено";
                    break;
            }
        } else if (entity instanceof Response) {
            Response response = (Response) entity;
            Vacancy vacancy = response.getVacancy();
            Candidate candidate = response.getCandidate();

            switch (eventType) {
                case "UPDATE":
                    message = String.format("Отклик кандидата %s %s на вакансию обновлен", sender.getFirstName(), sender.getLastName());
                    details = String.format("Обновлена вакансия: %s", vacancy.getPosition());
                    break;
                case "DELETE":
                    message = String.format("Отклик кандидата %s %s на вакансию удален", sender.getFirstName(), sender.getLastName());
                    details = "Отклик удален";
                    break;
            }
        }

        if (!message.isEmpty()) {
            Notification notification = createNotification(message, details, sender, recipient);
            if (entity instanceof Invite) {
                ((Invite) entity).setNotification(notification);
                inviteRepository.save((Invite) entity);
            } else if (entity instanceof Response) {
                ((Response) entity).setNotification(notification);
                responseRepository.save((Response) entity);
            }
        }
    }

    public List<Notification> getChat(Integer user1, Integer user2) {
        logger.info("Retrieving chat between user1={} and user2={}", user1, user2);
        List<Notification> notifications = notificationRepository.findChatBetween(user1, user2);
        logger.info("Found {} messages", notifications.size());
        return notifications;
    }

    public List<User> getChatRecipients(Integer userId) {
        logger.info("Retrieving chat recipients for userId={}", userId);
        List<Notification> notifs = notificationRepository.findBySender_UsersIdOrRecipient_UsersId(userId, userId);
        logger.info("Found {} notifications for userId={}", notifs.size(), userId);

        List<User> recipients = notifs.stream()
                .flatMap(n -> Stream.of(n.getSender(), n.getRecipient()))
                .filter(u -> u != null && !u.getUsersId().equals(userId))
                .distinct()
                .collect(Collectors.toList());

        logger.info("Found {} unique recipients", recipients.size());
        return recipients;
    }

    public Optional<Notification> findById(Integer notificationId) {
        logger.info("Finding notification: notificationId={}", notificationId);
        return notificationRepository.findById(notificationId);
    }

    public void editNotification(Integer notificationId, Integer userId, String details) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    logger.error("Notification not found: notificationId={}", notificationId);
                    return new RuntimeException("Notification not found");
                });

        if (!notification.getSender().getUsersId().equals(userId)) {
            logger.warn("Attempt to edit someone else's notification: notificationId={}, userId={}", notificationId, userId);
            throw new RuntimeException("No permission to edit");
        }

        notification.setDetails(details);
        notificationRepository.save(notification);
        logger.info("Notification edited: notificationId={}", notificationId);
    }

    public void editInviteNotification(Integer notificationId, Integer userId, LocalDateTime date) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    logger.error("Notification not found: notificationId={}", notificationId);
                    return new RuntimeException("Notification not found");
                });

        if (!notification.getSender().getUsersId().equals(userId)) {
            logger.warn("Attempt to edit someone else's notification: notificationId={}, userId={}", notificationId, userId);
            throw new RuntimeException("No permission to edit");
        }

        Optional<Invite> inviteOpt = inviteRepository.findByNotification_NotificationId(notificationId);
        if (inviteOpt.isEmpty()) {
            logger.error("Invite not found for notification: notificationId={}", notificationId);
            throw new RuntimeException("Invite not found");
        }

        Invite invite = inviteOpt.get();
        invite.setDate(date);
        inviteRepository.save(invite);
        notificationPublisher.notifyObservers("UPDATE", invite, notification.getSender(), notification.getRecipient());
        logger.info("Invite edited: notificationId={}, date={}", notificationId, date);
    }

    public void editResponseNotification(Integer notificationId, Integer userId, String vacancyName) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    logger.error("Notification not found: notificationId={}", notificationId);
                    return new RuntimeException("Notification not found");
                });

        if (!notification.getSender().getUsersId().equals(userId)) {
            logger.warn("Attempt to edit someone else's notification: notificationId={}, userId={}", notificationId, userId);
            throw new RuntimeException("No permission to edit");
        }

        Optional<Response> responseOpt = responseRepository.findByNotification_NotificationId(notificationId);
        if (responseOpt.isEmpty()) {
            logger.error("Response not found for notification: notificationId={}", notificationId);
            throw new RuntimeException("Response not found");
        }

        Vacancy vacancy = vacancyRepository.findByPosition(vacancyName)
                .orElseThrow(() -> {
                    logger.error("Vacancy not found: vacancyName={}", vacancyName);
                    return new RuntimeException("Vacancy not found");
                });

        Response response = responseOpt.get();
        response.setVacancy(vacancy);
        responseRepository.save(response);
        notificationPublisher.notifyObservers("UPDATE", response, notification.getSender(), notification.getRecipient());
        logger.info("Response edited: notificationId={}, vacancyName={}", notificationId, vacancyName);
    }

    public boolean vacancyExists(String vacancyName) {
        return vacancyRepository.findByPosition(vacancyName).isPresent();
    }

    @Transactional
    public void deleteNotification(Integer notificationId, Integer userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    logger.error("Notification not found: notificationId={}", notificationId);
                    return new RuntimeException("Notification not found");
                });

        if (!notification.getSender().getUsersId().equals(userId)) {
            logger.warn("Attempt to delete someone else's notification: notificationId={}, userId={}", notificationId, userId);
            throw new RuntimeException("No permission to delete notification");
        }

        Optional<Invite> inviteOpt = inviteRepository.findByNotification_NotificationId(notificationId);
        inviteOpt.ifPresent(invite -> {
            notificationPublisher.notifyObservers("DELETE", invite, notification.getSender(), notification.getRecipient());
            inviteRepository.delete(invite);
        });

        Optional<Response> responseOpt = responseRepository.findByNotification_NotificationId(notificationId);
        responseOpt.ifPresent(response -> {
            notificationPublisher.notifyObservers("DELETE", response, notification.getSender(), notification.getRecipient());
            responseRepository.delete(response);
        });

        notificationRepository.delete(notification);
        logger.info("Notification deleted: notificationId={}", notificationId);
    }

    public void updateNotificationResponse(Integer notificationId, Integer userId, String response) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> {
                    logger.error("Notification not found: notificationId={}", notificationId);
                    return new RuntimeException("Notification not found");
                });

        if (!notification.getRecipient().getUsersId().equals(userId)) {
            logger.warn("Attempt to respond to someone else's notification: notificationId={}, userId={}", notificationId, userId);
            throw new RuntimeException("No permission to respond");
        }

        notification.setResponse(response);
        notificationRepository.save(notification);
        logger.info("Response updated: notificationId={}, response={}", notificationId, response);
    }
}