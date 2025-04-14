package com.example.server.Repository;

import com.example.server.Models.Invite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InviteRepository extends JpaRepository<Invite, Integer> {
    Optional<Invite> findByNotification_NotificationId(Integer notificationId);
}