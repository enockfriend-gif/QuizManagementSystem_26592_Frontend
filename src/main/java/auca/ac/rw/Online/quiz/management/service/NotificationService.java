package auca.ac.rw.Online.quiz.management.service;

import auca.ac.rw.Online.quiz.management.model.Notification;
import auca.ac.rw.Online.quiz.management.model.User;
import auca.ac.rw.Online.quiz.management.repository.NotificationRepository;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public void createNotification(User user, String title, String message) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setCreatedAt(OffsetDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }

    public void notifyQuizPublished(User user, String quizTitle) {
        createNotification(user, "New Quiz Available",
                "A new quiz '" + quizTitle + "' has been published and is available for you to take.");
    }

    public void notifyQuizGraded(User user, String quizTitle, int score) {
        createNotification(user, "Quiz Graded",
                "Your quiz '" + quizTitle + "' has been graded. Score: " + score + "%");
    }
}