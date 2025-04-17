package com.example.server.Service;

import com.example.server.Models.Candidate;
import com.example.server.Models.Interview;
import com.example.server.Models.User;
import com.example.server.Models.Vacancy;
import com.example.server.Repository.InterviewRepository;
import com.example.server.Repository.UserRepository;
import com.example.server.Service.observer.NotificationPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InterviewService {
    private static final Logger logger = LoggerFactory.getLogger(InterviewService.class);

    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final NotificationPublisher notificationPublisher;

    public InterviewService(
            InterviewRepository interviewRepository,
            UserRepository userRepository,
            NotificationPublisher notificationPublisher
    ) {
        this.interviewRepository = interviewRepository;
        this.userRepository = userRepository;
        this.notificationPublisher = notificationPublisher;
    }

    public List<Interview> getInterviewsForUser(String email) {
        logger.info("Fetching interviews for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        if ("Кандидат".equals(user.getRole())) {
            return interviewRepository.findByUserEmail(email);
        } else if ("HR".equals(user.getRole())) {
            return interviewRepository.findByUserEmail(email);
        } else {
            throw new RuntimeException("Invalid user role");
        }
    }

    @Transactional
    public void deleteInterview(Integer interviewId, String email, String role) {
        logger.info("Deleting interview: id={}, userEmail={}, role={}", interviewId, email, role);
        Optional<Interview> interviewOpt = interviewRepository.findById(interviewId);
        if (interviewOpt.isEmpty()) {
            logger.error("Interview not found: id={}", interviewId);
            throw new RuntimeException("Interview not found");
        }

        Interview interview = interviewOpt.get();
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User recipient;

        // Определяем получателя уведомления в зависимости от роли
        if ("Кандидат".equals(role)) {
            if (!interview.getCandidate().getUser().getEmail().equals(email)) {
                logger.warn("Candidate {} not authorized to delete interview {}", email, interviewId);
                throw new RuntimeException("Not authorized to delete this interview");
            }
            recipient = interview.getVacancy().getEmployee().getUser();
        } else if ("HR".equals(role)) {
            if (!interview.getVacancy().getEmployee().getUser().getEmail().equals(email)) {
                logger.warn("HR {} not authorized to delete interview {}", email, interviewId);
                throw new RuntimeException("Not authorized to delete this interview");
            }
            recipient = interview.getCandidate().getUser();
        } else {
            logger.error("Invalid role: {}", role);
            throw new RuntimeException("Invalid role");
        }

        // Удаляем собеседование
        interviewRepository.delete(interview);
        logger.info("Interview deleted: id={}", interviewId);

        // Отправляем уведомление
        notificationPublisher.notifyObservers(
                "INTERVIEW_CANCELLED",
                interview,
                sender,
                recipient
        );
        logger.info("Notification sent for INTERVIEW_CANCELLED: interviewId={}, sender={}, recipient={}",
                interviewId, sender.getEmail(), recipient.getEmail());
    }

    // Другие методы, например createInterview
    @Transactional
    public void createInterview(Candidate candidate, Vacancy vacancy, LocalDateTime date) {
        logger.info("Creating interview for candidate: {}, vacancy: {}, date: {}", candidate.getUser().getEmail(), vacancy.getPosition(), date);
        Interview interview = new Interview();
        interview.setCandidate(candidate);
        interview.setVacancy(vacancy);
        interview.setDate(date);
        interview.setVacancy(vacancy);
        interviewRepository.save(interview);
        logger.info("Interview created: id={}", interview.getInterviewId());
    }
}