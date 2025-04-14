package com.example.server.Service;

import com.example.server.Models.Candidate;
import com.example.server.Models.Resume;
import com.example.server.Models.User;
import com.example.server.Repository.CandidateRepository;
import com.example.server.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
@Service
@RequiredArgsConstructor
@Transactional
public class ResumeService {
    private final CandidateRepository candidateRepository;
    private final UserRepository userRepository;

    public Resume createResume(Candidate candidate, Map<String, String> resumeData) {
        if (candidate.getResume() != null) {
            throw new IllegalStateException("Resume already exists. Use PUT to update.");
        }

        Resume resume = new Resume();
        resume.setSkills(resumeData.getOrDefault("skills", ""));
        resume.setEducation(resumeData.getOrDefault("education", ""));
        resume.setLanguages(resumeData.getOrDefault("languages", ""));
        resume.setCertifications(resumeData.getOrDefault("certifications", ""));
        resume.setProjects(resumeData.getOrDefault("projects", ""));
        resume.setResponsibilities(resumeData.getOrDefault("responsibilities", ""));
        resume.setCampaigns(resumeData.getOrDefault("campaigns", ""));

        candidate.setResume(resume);
        candidateRepository.save(candidate);

        return resume;
    }

    public Candidate updateResume(String email, Map<String, String> resumeData) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Candidate candidate = candidateRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Candidate not found"));

        Resume resume = candidate.getResume();
        if (resume == null) {
            throw new IllegalStateException("Resume not found. Use POST to create.");
        }

        resume.setSkills(resumeData.getOrDefault("skills", ""));
        resume.setEducation(resumeData.getOrDefault("education", ""));
        resume.setLanguages(resumeData.getOrDefault("languages", ""));
        resume.setCertifications(resumeData.getOrDefault("certifications", ""));
        resume.setProjects(resumeData.getOrDefault("projects", ""));
        resume.setResponsibilities(resumeData.getOrDefault("responsibilities", ""));
        resume.setCampaigns(resumeData.getOrDefault("campaigns", ""));

        return candidateRepository.save(candidate);
    }
}


