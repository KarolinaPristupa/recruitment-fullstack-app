package com.example.server.Service;

import com.example.server.Models.*;
import com.example.server.Repository.InterviewRepository;
import com.example.server.Repository.UserRepository;
import com.example.server.Repository.VacancyRepository;
import com.example.server.Service.observer.NotificationPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InterviewServiceTest {

    @Mock
    private InterviewRepository interviewRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VacancyRepository vacancyRepository;
    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private InterviewService interviewService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createInterview_savesInterview() {
        Candidate candidate = new Candidate();
        User user = new User();
        user.setEmail("candidate@mail.com");
        candidate.setUser(user);

        Vacancy vacancy = new Vacancy();
        vacancy.setPosition("Java Dev");

        LocalDateTime date = LocalDateTime.now().plusDays(1);
        Interview interview = new Interview();
        interview.setDate(date);

        when(interviewRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        interviewService.createInterview(candidate, vacancy, date);

        verify(interviewRepository, times(1)).save(any(Interview.class));
    }

    @Test
    void getInterviewsForUser_returnsInterviewsForCandidate() {
        User user = new User();
        user.setEmail("candidate@mail.com");
        user.setRole("Кандидат");

        List<Interview> interviews = List.of(new Interview());
        when(userRepository.findByEmail("candidate@mail.com")).thenReturn(Optional.of(user));
        when(interviewRepository.findByUserEmail("candidate@mail.com")).thenReturn(interviews);

        List<Interview> result = interviewService.getInterviewsForUser("candidate@mail.com");
        assertEquals(1, result.size());
    }

    @Test
    void deleteInterview_byCandidate_success() {
        String email = "candidate@mail.com";

        User user = new User(); user.setEmail(email); user.setRole("CANDIDATE");
        Candidate candidate = new Candidate(); candidate.setUser(user);
        User hrUser = new User(); hrUser.setEmail("hr@mail.com");
        Employee hr = new Employee(); hr.setUser(hrUser);
        Vacancy vacancy = new Vacancy(); vacancy.setEmployee(hr);

        Interview interview = new Interview();
        interview.setInterviewId(1);
        interview.setCandidate(candidate);
        interview.setVacancy(vacancy);

        when(interviewRepository.findById(1)).thenReturn(Optional.of(interview));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        interviewService.deleteInterview(1, email, "CANDIDATE");

        verify(interviewRepository).delete(interview);
        verify(notificationPublisher).notifyObservers(eq("INTERVIEW_CANCELLED"), any(), eq(user), eq(hrUser));
    }

    @Test
    void updateInterview_byHR_success() {
        String email = "hr@mail.com";
        User hrUser = new User(); hrUser.setEmail(email); hrUser.setRole("HR");
        User candidateUser = new User(); candidateUser.setEmail("cand@mail.com");

        Employee hr = new Employee(); hr.setUser(hrUser);
        Candidate candidate = new Candidate(); candidate.setUser(candidateUser);

        Vacancy vacancy = new Vacancy(); vacancy.setEmployee(hr);
        Interview interview = new Interview(); interview.setInterviewId(1); interview.setVacancy(vacancy); interview.setCandidate(candidate);

        when(interviewRepository.findById(1)).thenReturn(Optional.of(interview));
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(hrUser));
        when(vacancyRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);
        when(interviewRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Interview result = interviewService.updateInterview(1, email, "HR", LocalDateTime.now().plusDays(2), "New Position");

        assertEquals("New Position", vacancy.getPosition());
        verify(notificationPublisher).notifyObservers(eq("INTERVIEW_UPDATED"), any(), eq(hrUser), eq(candidateUser));
    }

    @Test
    void updateInterview_withPastDate_throwsException() {
        User user = new User(); user.setEmail("hr@mail.com"); user.setRole("HR");
        Employee employee = new Employee(); employee.setUser(user);
        Vacancy vacancy = new Vacancy(); vacancy.setEmployee(employee);
        Candidate candidate = new Candidate(); candidate.setUser(new User());

        Interview interview = new Interview();
        interview.setCandidate(candidate);
        interview.setVacancy(vacancy);

        when(interviewRepository.findById(1)).thenReturn(Optional.of(interview));
        when(userRepository.findByEmail("hr@mail.com")).thenReturn(Optional.of(user));

        LocalDateTime past = LocalDateTime.now().minusDays(1);

        RuntimeException e = assertThrows(RuntimeException.class,
                () -> interviewService.updateInterview(1, "hr@mail.com", "HR", past, "New"));

        assertEquals("Interview date must be in the future", e.getMessage());
    }
}
