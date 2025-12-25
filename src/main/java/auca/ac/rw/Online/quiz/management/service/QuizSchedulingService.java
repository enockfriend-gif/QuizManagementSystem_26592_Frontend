package auca.ac.rw.Online.quiz.management.service;

import auca.ac.rw.Online.quiz.management.model.Quiz;
import auca.ac.rw.Online.quiz.management.model.EQuizStatus;
import auca.ac.rw.Online.quiz.management.repository.QuizRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class QuizSchedulingService {
    private final QuizRepository quizRepository;

    public QuizSchedulingService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    @Scheduled(fixedRate = 60000) // Check every minute
    public void updateQuizStatuses() {
        OffsetDateTime now = OffsetDateTime.now();
        
        // Auto-publish quizzes that should start
        List<Quiz> quizzesToPublish = quizRepository.findByStatusAndStartTimeBefore(
            EQuizStatus.DRAFT, now);
        for (Quiz quiz : quizzesToPublish) {
            if (quiz.getStartTime() != null && quiz.getStartTime().isBefore(now)) {
                quiz.setStatus(EQuizStatus.PUBLISHED);
                quizRepository.save(quiz);
            }
        }
        
        // Auto-archive quizzes that should end
        List<Quiz> quizzesToArchive = quizRepository.findByStatusAndEndTimeBefore(
            EQuizStatus.PUBLISHED, now);
        for (Quiz quiz : quizzesToArchive) {
            if (quiz.getEndTime() != null && quiz.getEndTime().isBefore(now)) {
                quiz.setStatus(EQuizStatus.ARCHIVED);
                quizRepository.save(quiz);
            }
        }
    }

    public boolean isQuizAvailable(Quiz quiz) {
        OffsetDateTime now = OffsetDateTime.now();
        
        if (quiz.getStatus() != EQuizStatus.PUBLISHED) {
            return false;
        }
        
        if (quiz.getStartTime() != null && quiz.getStartTime().isAfter(now)) {
            return false;
        }
        
        if (quiz.getEndTime() != null && quiz.getEndTime().isBefore(now)) {
            return false;
        }
        
        return true;
    }
}