package com.example.server.Service.observer;

import com.example.server.Models.User;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Component
public class NotificationPublisher implements Observable {
    private static final Logger logger = Logger.getLogger(NotificationPublisher.class.getName());
    private final List<Observer> observers = new ArrayList<>();

    public void subscribe(Observer observer) {
        observers.add(observer);
        logger.info("Observer subscribed: {}");
    }

    public void unsubscribe(Observer observer) {
        observers.remove(observer);
        logger.info("Observer unsubscribed: {}");
    }

    @Override
    public void notifyObservers(String eventType, Object entity, User sender, User recipient) {
        logger.info("Notifying observers for event: {}, entity: {}, sender: {}, recipient: {}"
        );
        for (Observer observer : observers) {
            observer.update(eventType, entity, sender, recipient);
        }
        logger.info("Notified {} observers for event: {}");
    }

    public void notifyObservers(String eventType, Object entity, User sender, User recipient, String message, String details) {
        logger.info("Notifying observers for event: {}, entity: {}, sender: {}, recipient: {}, message: {}, details: {}"
        );
        for (Observer observer : observers) {
            observer.update(eventType, entity, sender, recipient, message, details);
        }
        logger.info("Notified {} observers for event: {}");
    }
}