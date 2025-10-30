package auca.ac.rw.Online.quiz.management.service;

import auca.ac.rw.Online.quiz.management.model.Notification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {
    private final List<Notification> inMemoryNotifications = new ArrayList<>();

    public List<Notification> findAll() {
        return inMemoryNotifications;
    }

    public Notification send(Notification notification) {
        inMemoryNotifications.add(notification);
        return notification;
    }
}


