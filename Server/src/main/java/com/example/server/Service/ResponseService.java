package com.example.server.Service;

import com.example.server.Models.*;
import com.example.server.Repository.ResponseRepository;
import com.example.server.Service.observer.NotificationPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ResponseService {

    private static final Logger logger = LoggerFactory.getLogger(ResponseService.class);

    private final ResponseRepository responseRepository;
    private final UserService userService;
    private final VacancyService vacancyService;
    private final CandidateService candidateService;
    private final NotificationService notificationService;
    private final NotificationPublisher notificationPublisher;

    public ResponseService(
            ResponseRepository responseRepository,
            UserService userService,
            CandidateService candidateService,
            VacancyService vacancyService,
            NotificationService notificationService,
            NotificationPublisher notificationPublisher
    ) {
        this.responseRepository = responseRepository;
        this.userService = userService;
        this.candidateService = candidateService;
        this.vacancyService = vacancyService;
        this.notificationService = notificationService;
        this.notificationPublisher = notificationPublisher;
    }

    @Transactional
    public Response createResponse(String candidateEmail, Integer vacancyId) {
        logger.info("Creating response: candidateEmail={}, vacancyId={}", candidateEmail, vacancyId);

        User user = userService.findByEmail(candidateEmail)
                .orElseThrow(() -> {
                    logger.error("Candidate not found: {}", candidateEmail);
                    return new RuntimeException("Candidate not found");
                });

        Candidate candidate = candidateService.getCandidateProfileOrEmpty(candidateEmail);

        Vacancy vacancy = vacancyService.findById(vacancyId);
        if (!vacancy.getStatus().equalsIgnoreCase("Активно")) {
            logger.warn("Vacancy not active: vacancyId={}", vacancyId);
            throw new RuntimeException("Vacancy not active");
        }

        User creator = vacancy.getEmployee().getUser();
        if (creator == null) {
            logger.error("Vacancy creator not found for vacancyId={}", vacancyId);
            throw new RuntimeException("Vacancy creator not found");
        }

        // Create notification explicitly
        String message = String.format("Кандидат %s %s откликнулся на вакансию", user.getFirstName(), user.getLastName());
        String details = vacancy.getPosition();
        Notification notification = notificationService.createNotification(message, details, user, creator);

        Response response = new Response();
        response.setCandidate(candidate);
        response.setVacancy(vacancy);
        response.setNotification(notification); // Set notification before saving

        response = responseRepository.save(response);
        notificationPublisher.notifyObservers("CREATE", response, user, creator);
        logger.info("Created response: responseId={}", response.getResponseId());

        return response;
    }

    @Transactional
    public Response updateResponse(Integer responseId, String vacancyName, User sender, User recipient) {
        Response response = responseRepository.findById(responseId)
                .orElseThrow(() -> {
                    logger.error("Response not found: responseId={}", responseId);
                    return new RuntimeException("Response not found");
                });

        Vacancy vacancy = vacancyService.findByPosition(vacancyName);

        response.setVacancy(vacancy);
        response = responseRepository.save(response);
        notificationPublisher.notifyObservers("UPDATE", response, sender, recipient);
        logger.info("Updated response: responseId={}", response.getResponseId());

        return response;
    }

    @Transactional
    public void deleteResponse(Integer responseId, User sender, User recipient) {
        Response response = responseRepository.findById(responseId)
                .orElseThrow(() -> {
                    logger.error("Response not found: responseId={}", responseId);
                    return new RuntimeException("Response not found");
                });

        notificationPublisher.notifyObservers("DELETE", response, sender, recipient);
        responseRepository.delete(response);
        logger.info("Deleted response: responseId={}", responseId);
    }

    public Response save(Response response) {
        return responseRepository.save(response);
    }
}