package com.example.server.Service;

import com.example.server.Models.Candidate;
import com.example.server.Models.Resume;
import com.example.server.Models.User;
import com.example.server.Repository.CandidateRepository;
import com.example.server.Repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ResumeServiceTest {

    private CandidateRepository candidateRepository;
    private UserRepository userRepository;
    private ResumeService resumeService;

    @BeforeEach
    void setUp() {
        candidateRepository = mock(CandidateRepository.class);
        userRepository = mock(UserRepository.class);
        resumeService = new ResumeService(candidateRepository, userRepository);
    }

    @Test
    void testCreateResumeSuccessfully() {
        Candidate candidate = new Candidate();
        candidate.setResume(null);

        Map<String, String> data = Map.of(
                "skills", "Java",
                "education", "BSc",
                "languages", "English"
        );

        Resume createdResume = resumeService.createResume(candidate, data);

        assertEquals("Java", createdResume.getSkills());
        assertEquals("BSc", createdResume.getEducation());
        assertEquals("English", createdResume.getLanguages());

        verify(candidateRepository).save(candidate);
    }

    @Test
    void testCreateResumeFailsWhenAlreadyExists() {
        Candidate candidate = new Candidate();
        candidate.setResume(new Resume()); // already exists

        Map<String, String> data = Map.of();

        assertThrows(IllegalStateException.class,
                () -> resumeService.createResume(candidate, data));
        verify(candidateRepository, never()).save(any());
    }

    @Test
    void testUpdateResumeSuccessfully() {
        String email = "test@example.com";

        User user = new User();
        Candidate candidate = new Candidate();
        Resume resume = new Resume();
        candidate.setResume(resume);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(candidateRepository.findByUser(user)).thenReturn(Optional.of(candidate));
        when(candidateRepository.save(any())).thenReturn(candidate);

        Map<String, String> data = Map.of("skills", "Spring");

        Candidate updatedCandidate = resumeService.updateResume(email, data);

        assertEquals("Spring", updatedCandidate.getResume().getSkills());
        verify(candidateRepository).save(candidate);
    }

    @Test
    void testUpdateResumeFailsWhenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> resumeService.updateResume("missing@example.com", Map.of()));
    }

    @Test
    void testUpdateResumeFailsWhenCandidateNotFound() {
        User user = new User();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(candidateRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> resumeService.updateResume("test@example.com", Map.of()));
    }

    @Test
    void testUpdateResumeFailsWhenResumeIsNull() {
        User user = new User();
        Candidate candidate = new Candidate();
        candidate.setResume(null);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(candidateRepository.findByUser(user)).thenReturn(Optional.of(candidate));

        assertThrows(IllegalStateException.class,
                () -> resumeService.updateResume("test@example.com", Map.of()));
    }
}
