package com.example.server.Service;

import com.example.server.Models.Candidate;
import com.example.server.Models.Interview;
import com.example.server.Models.User;
import com.example.server.Models.Vacancy;
import com.example.server.Repository.InterviewRepository;
import com.example.server.Repository.UserRepository;
import com.example.server.Repository.VacancyRepository;
import com.example.server.Service.observer.NotificationPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class InterviewService {
    private static final Logger logger = LoggerFactory.getLogger(InterviewService.class);

    private final InterviewRepository interviewRepository;
    private final UserRepository userRepository;
    private final VacancyRepository vacancyRepository;
    private final NotificationPublisher notificationPublisher;

    public InterviewService(
            InterviewRepository interviewRepository,
            UserRepository userRepository,
            VacancyRepository vacancyRepository,
            NotificationPublisher notificationPublisher
    ) {
        this.interviewRepository = interviewRepository;
        this.userRepository = userRepository;
        this.vacancyRepository = vacancyRepository;
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

        interviewRepository.delete(interview);
        logger.info("Interview deleted: id={}", interviewId);

        notificationPublisher.notifyObservers(
                "INTERVIEW_CANCELLED",
                interview,
                sender,
                recipient
        );
        logger.info("Notification sent for INTERVIEW_CANCELLED: interviewId={}, sender={}, recipient={}",
                interviewId, sender.getEmail(), recipient.getEmail());
    }

    @Transactional
    public Interview updateInterview(Integer interviewId, String email, String role, LocalDateTime newDate, String newPosition) {
        logger.info("Updating interview: id={}, userEmail={}, role={}, newDate={}, newPosition={}",
                interviewId, email, role, newDate, newPosition);
        Optional<Interview> interviewOpt = interviewRepository.findById(interviewId);
        if (interviewOpt.isEmpty()) {
            logger.error("Interview not found: id={}", interviewId);
            throw new RuntimeException("Interview not found");
        }

        Interview interview = interviewOpt.get();
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User recipient;

        if ("Кандидат".equals(role)) {
            if (!interview.getCandidate().getUser().getEmail().equals(email)) {
                logger.warn("Candidate {} not authorized to update interview {}", email, interviewId);
                throw new RuntimeException("Not authorized to update this interview");
            }
            recipient = interview.getVacancy().getEmployee().getUser();
        } else if ("HR".equals(role)) {
            if (!interview.getVacancy().getEmployee().getUser().getEmail().equals(email)) {
                logger.warn("HR {} not authorized to update interview {}", email, interviewId);
                throw new RuntimeException("Not authorized to update this interview");
            }
            recipient = interview.getCandidate().getUser();
        } else {
            logger.error("Invalid role: {}", role);
            throw new RuntimeException("Invalid role");
        }

        // Валидация даты
        if (newDate != null) {
            if (newDate.isBefore(LocalDateTime.now())) {
                logger.error("Interview date must be in the future: {}", newDate);
                throw new RuntimeException("Interview date must be in the future");
            }
            interview.setDate(newDate);
        }

        // Обновление позиции через Vacancy
        if (newPosition != null && !newPosition.trim().isEmpty()) {
            Vacancy vacancy = interview.getVacancy();
            if (vacancy == null) {
                logger.error("Vacancy not found for interview: id={}", interviewId);
                throw new RuntimeException("Vacancy not found");
            }
            vacancy.setPosition(newPosition);
            vacancyRepository.save(vacancy);
            logger.info("Vacancy updated: id={}, newPosition={}", vacancy.getVacancies_id(), newPosition);
        }

        Interview updatedInterview = interviewRepository.save(interview);
        logger.info("Interview updated: id={}", interviewId);

        // Отправляем уведомление
        notificationPublisher.notifyObservers(
                "INTERVIEW_UPDATED",
                updatedInterview,
                sender,
                recipient
        );
        logger.info("Notification sent for INTERVIEW_UPDATED: interviewId={}, sender={}, recipient={}",
                interviewId, sender.getEmail(), recipient.getEmail());

        return updatedInterview;
    }

    @Transactional
    public void createInterview(Candidate candidate, Vacancy vacancy, LocalDateTime date) {
        logger.info("Creating interview for candidate: {}, vacancy: {}, date: {}",
                candidate.getUser().getEmail(), vacancy.getPosition(), date);
        Interview interview = new Interview();
        interview.setCandidate(candidate);
        interview.setVacancy(vacancy);
        interview.setDate(date);
        interviewRepository.save(interview);
        logger.info("Interview created: id={}", interview.getInterviewId());
    }

    @Transactional
    public Interview updateInterviewResult(Integer interviewId, String email, String role, String newResult) {
        logger.info("Updating interview result: id={}, userEmail={}, role={}, newResult={}", interviewId, email, role, newResult);
        Optional<Interview> interviewOpt = interviewRepository.findById(interviewId);
        if (interviewOpt.isEmpty()) {
            logger.error("Interview not found: id={}", interviewId);
            throw new RuntimeException("Interview not found");
        }

        Interview interview = interviewOpt.get();
        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User recipient;

        if ("HR".equals(role)) {
            if (!interview.getVacancy().getEmployee().getUser().getEmail().equals(email)) {
                logger.warn("HR {} not authorized to update interview {}", email, interviewId);
                throw new RuntimeException("Not authorized to update this interview");
            }
            recipient = interview.getCandidate().getUser();
        } else {
            logger.error("Invalid role for updating result: {}", role);
            throw new RuntimeException("Only HR can update interview result");
        }

        if (!Arrays.asList("Принят", "Отклонён").contains(newResult)) {
            logger.error("Invalid result value: {}", newResult);
            throw new RuntimeException("Result must be 'Принят' or 'Отклонён'");
        }

        interview.setResult(newResult);
        Interview updatedInterview = interviewRepository.save(interview);
        logger.info("Interview result updated: id={}", interviewId);

        notificationPublisher.notifyObservers(
                "INTERVIEW_RESULT_UPDATED",
                updatedInterview,
                sender,
                recipient
        );
        logger.info("Notification sent for INTERVIEW_RESULT_UPDATED: interviewId={}, sender={}, recipient={}",
                interviewId, sender.getEmail(), recipient.getEmail());

        return updatedInterview;
    }


}