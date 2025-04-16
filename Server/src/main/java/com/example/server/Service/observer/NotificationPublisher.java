package com.example.server.Service.observer;

import com.example.server.Models.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationPublisher implements Observable {
    private final List<Observer> observers = new ArrayList<>();

    public void subscribe(Observer observer) {
        observers.add(observer);
    }

    public void unsubscribe(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(String eventType, Object entity, User sender, User recipient) {
        for (Observer observer : observers) {
            observer.update(eventType, entity, sender, recipient);
        }
    }

    public void notifyObservers(String eventType, Object entity, User sender, User recipient, String message, String details) {
        for (Observer observer : observers) {
            observer.update(eventType, entity, sender, recipient, message, details);
        }
    }
}