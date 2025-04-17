package com.example.server.Service;

import com.example.server.Models.Candidate;
import com.example.server.Models.Interview;
import com.example.server.Models.Vacancy;
import com.example.server.Repository.InterviewRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InterviewService {
    private static final Logger logger = LoggerFactory.getLogger(InterviewService.class);
    private final InterviewRepository interviewRepository;

    public InterviewService(InterviewRepository interviewRepository) {
        this.interviewRepository = interviewRepository;
    }

    @Transactional
    public Interview createInterview(Candidate candidate, Vacancy vacancy, LocalDateTime date) {
        Interview interview = new Interview();
        interview.setCandidate(candidate);
        interview.setVacancy(vacancy);
        interview.setDate(date);
        interview.setResult("Ожидание"); // Default result
        return interviewRepository.save(interview);
    }

    public List<Interview> getInterviewsForUser(String email) {
        logger.info("Fetching interviews for user: {}", email);
        List<Interview> interviews = interviewRepository.findByUserEmail(email);
        logger.debug("Fetched {} interviews", interviews.size());
        return interviews;
    }
}