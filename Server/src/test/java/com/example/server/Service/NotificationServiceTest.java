package com.example.server.Service;

import com.example.server.Models.*;
import com.example.server.Repository.*;
import com.example.server.Service.observer.NotificationPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InviteRepository inviteRepository;

    @Mock
    private ResponseRepository responseRepository;

    @Mock
    private VacancyRepository vacancyRepository;

    @Mock
    private InterviewService interviewService;

    @Mock
    private NotificationPublisher notificationPublisher;

    @InjectMocks
    private NotificationService notificationService;

    private User sender;
    private User recipient;
    private Notification notification;
    private Invite invite;
    private Response response;
    private Vacancy vacancy;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        sender = new User();
        sender.setUsersId(1);
        sender.setFirstName("John");
        sender.setLastName("Doe");

        recipient = new User();
        recipient.setUsersId(2);
        recipient.setFirstName("Jane");
        recipient.setLastName("Smith");

        notification = new Notification();
        notification.setNotificationId(1);
        notification.setSender(sender);
        notification.setRecipient(recipient);
        notification.setMessage("Test Message");
        notification.setDetails("Test Details");

        vacancy = new Vacancy();
        vacancy.setPosition("Software Engineer");

        invite = new Invite();
        invite.setVacancy(vacancy);
        invite.setCandidate(new Candidate());
        invite.setDate(LocalDateTime.now());

        response = new Response();
        response.setVacancy(vacancy);
        response.setCandidate(new Candidate());
    }

    @Test
    void testCreateNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification createdNotification = notificationService.createNotification(
                "Test Message", "Test Details", sender, recipient);

        assertNotNull(createdNotification);
        assertEquals("Test Message", createdNotification.getMessage());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testSaveNotification() {
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);

        Notification savedNotification = notificationService.save(notification);

        assertNotNull(savedNotification);
        assertEquals(notification.getNotificationId(), savedNotification.getNotificationId());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testDeleteNotification() {
        when(notificationRepository.findById(1)).thenReturn(Optional.of(notification));
        when(inviteRepository.findByNotification_NotificationId(1)).thenReturn(Optional.of(invite));

        notificationService.deleteNotification(1, sender.getUsersId());

        verify(notificationRepository, times(1)).delete(any(Notification.class));
        verify(inviteRepository, times(1)).delete(any(Invite.class));
        verify(notificationPublisher, times(1)).notifyObservers(eq("DELETE"), any(), eq(sender), eq(recipient));
    }

    @Test
    void testEditNotification() {
        when(notificationRepository.findById(1)).thenReturn(Optional.of(notification));

        notificationService.editNotification(1, sender.getUsersId(), "Updated Details");

        assertEquals("Updated Details", notification.getDetails());
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    void testVacancyExists() {
        when(vacancyRepository.findByPosition("Software Engineer")).thenReturn(Optional.of(vacancy));

        boolean exists = notificationService.vacancyExists("Software Engineer");

        assertTrue(exists);
        verify(vacancyRepository, times(1)).findByPosition("Software Engineer");
    }

    @Test
    void testGetChat() {
        when(notificationRepository.findChatBetween(1, 2)).thenReturn(List.of(notification));

        List<Notification> chat = notificationService.getChat(1, 2);

        assertNotNull(chat);
        assertEquals(1, chat.size());
        verify(notificationRepository, times(1)).findChatBetween(1, 2);
    }

    @Test
    void testGetChatRecipients() {
        when(notificationRepository.findBySender_UsersIdOrRecipient_UsersId(1, 1)).thenReturn(List.of(notification));

        List<User> recipients = notificationService.getChatRecipients(1);

        assertNotNull(recipients);
        assertEquals(1, recipients.size());
        verify(notificationRepository, times(1)).findBySender_UsersIdOrRecipient_UsersId(1, 1);
    }
}
