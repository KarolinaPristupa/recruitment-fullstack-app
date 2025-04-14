package com.example.server.Service;

import com.example.server.Models.*;
import com.example.server.Repository.ResponseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResponseService {

    private static final Logger logger = LoggerFactory.getLogger(ResponseService.class);

    private final ResponseRepository responseRepository;
    private final NotificationService notificationService;
    private final UserService userService;
    private final VacancyService vacancyService;

    private final CandidateService candidateService;

    @Autowired
    public ResponseService(ResponseRepository responseRepository,
                           NotificationService notificationService,
                           UserService userService,
                           CandidateService candidateService,
                           VacancyService vacancyService) {
        this.responseRepository = responseRepository;
        this.notificationService = notificationService;
        this.userService = userService;
        this.candidateService = candidateService;
        this.vacancyService = vacancyService;
    }

    public Response createResponse(String candidateEmail, Integer vacancyId) {
        logger.info("Создание отклика: candidateEmail={}, vacancyId={}", candidateEmail, vacancyId);

        // Найти кандидата
        User user = userService.findByEmail(candidateEmail)
                .orElseThrow(() -> {
                    logger.error("Кандидат не найден: {}", candidateEmail);
                    return new RuntimeException("Кандидат не найден");
                });

        Candidate candidate = candidateService.getCandidateProfileOrEmpty(candidateEmail);

        // Найти вакансию
        Vacancy vacancy = vacancyService.findById(vacancyId);
        if (!vacancy.getStatus().equalsIgnoreCase("Активно")) {
            logger.warn("Вакансия не активна: vacancyId={}", vacancyId);
            throw new RuntimeException("Вакансия не активна");
        }

        // Найти создателя вакансии
        User creator = vacancy.getEmployee().getUser();
        if (creator == null) {
            logger.error("Создатель вакансии не найден для vacancyId={}", vacancyId);
            throw new RuntimeException("Создатель вакансии не найден");
        }

        // Создать уведомление
        String message = String.format("Кандидат %s %s откликнулся на вакансию",
                user.getFirstName(), user.getLastName());
        String details = vacancy.getPosition();
        Notification notification = notificationService.createNotification(message, details, user, creator);
        logger.info("Создано уведомление: notificationId={}", notification.getNotificationId());

        // Создать отклик
        Response response = new Response();
        response.setCandidate(candidate);
        response.setVacancy(vacancy);
        response.setNotification(notification);

        Response savedResponse = responseRepository.save(response);
        logger.info("Создан отклик: responseId={}", savedResponse.getResponseId());

        return savedResponse;
    }

    public Response save(Response response) {
        return responseRepository.save(response);
    }
}