package com.example.server.Service.observer;

import com.example.server.Models.User;

public interface Observable {
    void notifyObservers(String eventType, Object entity, User sender, User recipient);
}
