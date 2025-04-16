package com.example.server.Service;

import com.example.server.Models.*;
import com.example.server.Repository.CandidateRepository;
import com.example.server.Repository.InviteRepository;
import com.example.server.Service.observer.NotificationPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class InviteService {
    private final InviteRepository inviteRepository;
    private final CandidateRepository candidateRepository;
    private final NotificationService notificationService;
    private final NotificationPublisher notificationPublisher;

    public InviteService(
            InviteRepository inviteRepository,
            CandidateRepository candidateRepository,
            NotificationService notificationService,
            NotificationPublisher notificationPublisher
    ) {
        this.inviteRepository = inviteRepository;
        this.candidateRepository = candidateRepository;
        this.notificationService = notificationService;
        this.notificationPublisher = notificationPublisher;
    }

    @Transactional
    public Invite createInvite(User sender, User recipient, Vacancy vacancy, LocalDateTime date) {
        Candidate candidate = candidateRepository.findByUser(recipient)
                .orElseThrow(() -> new RuntimeException("Candidate not found for user: " + recipient.getUsersId()));

        // Create notification explicitly
        String message = String.format("Вы приглашены на собеседование для вакансии: %s", vacancy.getPosition());
        String details = String.format("Дата: %s, Время: %s", date.toLocalDate(), date.toLocalTime().withSecond(0).withNano(0));
        Notification notification = notificationService.createNotification(message, details, sender, recipient);

        Invite invite = new Invite();
        invite.setNotification(notification); // Set notification before saving
        invite.setVacancy(vacancy);
        invite.setCandidate(candidate);
        invite.setDate(date);

        invite = inviteRepository.save(invite);
        notificationPublisher.notifyObservers("CREATE", invite, sender, recipient); // Notify observers
        return invite;
    }

    @Transactional
    public Invite updateInvite(Integer inviteId, LocalDateTime date, User sender, User recipient) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found: " + inviteId));
        invite.setDate(date);
        invite = inviteRepository.save(invite);
        notificationPublisher.notifyObservers("UPDATE", invite, sender, recipient);
        return invite;
    }

    @Transactional
    public void deleteInvite(Integer inviteId, User sender, User recipient) {
        Invite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new RuntimeException("Invite not found: " + inviteId));
        notificationPublisher.notifyObservers("DELETE", invite, sender, recipient);
        inviteRepository.delete(invite);
    }

    public Invite save(Invite invite) {
        return inviteRepository.save(invite);
    }

    public Optional<Invite> findByNotificationId(Integer notificationId) {
        return inviteRepository.findByNotification_NotificationId(notificationId);
    }
}