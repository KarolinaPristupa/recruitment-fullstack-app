package com.example.server.Repository;

import com.example.server.Models.Response;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResponseRepository extends JpaRepository<Response, Integer> {
    Optional<Response> findByNotification_NotificationId(Integer notificationId);
}