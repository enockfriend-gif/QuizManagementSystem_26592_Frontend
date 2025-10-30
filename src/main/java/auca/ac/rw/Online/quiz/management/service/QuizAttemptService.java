package auca.ac.rw.Online.quiz.management.service;

import auca.ac.rw.Online.quiz.management.model.QuizAttempt;
import auca.ac.rw.Online.quiz.management.repository.QuizAttemptRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuizAttemptService {
    private final QuizAttemptRepository quizAttemptRepository;

    public QuizAttemptService(QuizAttemptRepository quizAttemptRepository) {
        this.quizAttemptRepository = quizAttemptRepository;
    }

    public List<QuizAttempt> findAll() { return quizAttemptRepository.findAll(); }
    public Optional<QuizAttempt> findById(Long id) { return quizAttemptRepository.findById(id); }
    public QuizAttempt save(QuizAttempt attempt) { return quizAttemptRepository.save(attempt); }
    public void deleteById(Long id) { quizAttemptRepository.deleteById(id); }
}


