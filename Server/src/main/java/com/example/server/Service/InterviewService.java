package com.example.server.Service;

import com.example.server.Models.Candidate;
import com.example.server.Models.Interview;
import com.example.server.Models.Vacancy;
import com.example.server.Repository.InterviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InterviewService {

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
}