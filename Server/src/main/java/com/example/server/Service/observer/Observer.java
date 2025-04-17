package com.example.server.Service.observer;

import com.example.server.Models.User;

public interface Observer {
    void update(String eventType, Object entity, User sender, User recipient);
    void update(String eventType, Object entity, User sender, User recipient, String message, String details);
}