package com.example.server.Repository;

import com.example.server.Models.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByRecipient_UsersId(Integer recipientId);

    @Query("SELECT n FROM Notification n WHERE " +
            "(n.sender.usersId = :user1 AND n.recipient.usersId = :user2) OR " +
            "(n.sender.usersId = :user2 AND n.recipient.usersId = :user1) " +
            "ORDER BY n.notificationId ASC")
    List<Notification> findChatBetween(@Param("user1") Integer user1, @Param("user2") Integer user2);

    List<Notification> findBySender_UsersIdOrRecipient_UsersId(Integer senderId, Integer recipientId);
}
