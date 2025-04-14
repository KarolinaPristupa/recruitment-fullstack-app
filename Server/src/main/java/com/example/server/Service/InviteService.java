package com.example.server.Service;

import com.example.server.Models.*;
import com.example.server.Repository.InviteRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class InviteService {
    private final InviteRepository inviteRepository;
    private final NotificationService notificationService;

    public InviteService(InviteRepository inviteRepository, NotificationService notificationService) {
        this.inviteRepository = inviteRepository;
        this.notificationService = notificationService;
    }

    public Invite createInvite(User sender, User recipient, Vacancy vacancy, LocalDateTime date) {
        String message = String.format(
                "Вы приглашены на собеседование для вакансии: %s",
                vacancy.getPosition()
        );

        String details = String.format(
                "Дата: %s, Время: %s",
                date.toLocalDate(),
                date.toLocalTime().withSecond(0).withNano(0)
        );

        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setDetails(details);
        notification.setSender(sender);
        notification.setRecipient(recipient);
        notification.setResponse(null);

        Notification savedNotification = notificationService.save(notification);

        Invite invite = new Invite();
        invite.setNotification(savedNotification);
        invite.setVacancy(vacancy);
        invite.setDate(date);

        return inviteRepository.save(invite);
    }

    public Invite save(Invite invite) {
        return inviteRepository.save(invite);
    }
    public Optional<Invite> findByNotificationId(Integer notificationId) {
        return inviteRepository.findByNotification_NotificationId(notificationId);
    }
}
