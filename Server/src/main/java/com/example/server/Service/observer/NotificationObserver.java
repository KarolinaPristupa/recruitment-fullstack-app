package com.example.server.Service.observer;

import com.example.server.Models.Interview;
import com.example.server.Models.User;
import com.example.server.Service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationObserver implements Observer {
    private static final Logger logger = LoggerFactory.getLogger(NotificationObserver.class);
    private final NotificationService notificationService;
    private final NotificationPublisher notificationPublisher;

    public NotificationObserver(NotificationService notificationService, NotificationPublisher notificationPublisher) {
        this.notificationService = notificationService;
        this.notificationPublisher = notificationPublisher;
        this.notificationPublisher.subscribe(this); // Добавляем подписку
        logger.info("NotificationObserver subscribed to NotificationPublisher");
    }

    @Override
    public void update(String eventType, Object entity, User sender, User recipient) {
        update(eventType, entity, sender, recipient, null, null);
    }

    @Override
    public void update(String eventType, Object entity, User sender, User recipient, String message, String details) {
        logger.info("Processing event: {}, entity: {}, sender: {}, recipient: {}",
                eventType, entity, sender.getEmail(), recipient.getEmail());
        if (eventType.equals("INTERVIEW_UPDATED") || eventType.equals("INTERVIEW_CANCELLED")) {
            Interview interview = (Interview) entity;
            String notificationMessage = eventType.equals("INTERVIEW_UPDATED") ?
                    "Собеседование обновлено" : "Собеседование отменено";
            String notificationDetails = eventType.equals("INTERVIEW_UPDATED") ?
                    String.format("Дата собеседования на позицию %s изменена на %s", interview.getPosition(), interview.getDate()) :
                    String.format("Собеседование на позицию %s в %s было отменено", interview.getPosition(), interview.getDate());

            logger.info("Creating notification: message={}, details={}", notificationMessage, notificationDetails);
            notificationService.createNotification(
                    notificationMessage,
                    notificationDetails,
                    sender,
                    recipient
            );
            logger.info("Notification created for event: {}", eventType);
        }
    }
}