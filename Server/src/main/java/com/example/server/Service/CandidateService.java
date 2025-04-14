package com.example.server.Service;

import com.example.server.Models.Candidate;
import com.example.server.Models.Resume;
import com.example.server.Models.User;
import com.example.server.Repository.CandidateRepository;
import com.example.server.Repository.ResumeRepository;
import com.example.server.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CandidateService {
    private final UserRepository userRepository;
    private final CandidateRepository candidateRepository;

    @Autowired
    private final ResumeRepository resumeRepository;

    public Candidate getById(Integer id) {
        return candidateRepository.findById(id).orElse(null);
    }
    public Candidate getCandidateProfileOrEmpty(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return candidateRepository.findByUser(user).orElseGet(() -> {
            Candidate emptyCandidate = new Candidate();
            emptyCandidate.setUser(user);
            emptyCandidate.setResume(new Resume());
            return emptyCandidate;
        });
    }

    public Candidate getOrCreateCandidate(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return candidateRepository.findByUser(user).orElseGet(() -> {
            Candidate newCandidate = new Candidate();
            newCandidate.setUser(user);
            candidateRepository.save(newCandidate);
            candidateRepository.flush();
            return newCandidate;
        });
    }

    @Transactional
    public void deleteCandidateWithResume(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        candidateRepository.deleteByUser(user);
    }
    public Optional<Candidate> findById(Integer id) {
        return candidateRepository.findById(id);
    }

    public List<Candidate> getAllCandidates() {
        return candidateRepository.findAll();
    }
}

