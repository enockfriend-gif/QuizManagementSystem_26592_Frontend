package auca.ac.rw.Online.quiz.management.service;

import auca.ac.rw.Online.quiz.management.model.QuizAttempt;
import auca.ac.rw.Online.quiz.management.repository.QuizAttemptRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuizAttemptService {
    private final QuizAttemptRepository quizAttemptRepository;

    public QuizAttemptService(QuizAttemptRepository quizAttemptRepository) {
        this.quizAttemptRepository = quizAttemptRepository;
    }

    public List<QuizAttempt> findAll() {
        // Use findAllWithUserAndQuiz to ensure user and quiz are loaded for dashboard calculations
        return quizAttemptRepository.findAllWithUserAndQuiz();
    }

    public Page<QuizAttempt> findAll(Pageable pageable) {
        return quizAttemptRepository.findAll(pageable);
    }

    public Optional<QuizAttempt> findById(Long id) {
        return quizAttemptRepository.findById(id);
    }

    public QuizAttempt save(QuizAttempt attempt) {
        return quizAttemptRepository.save(attempt);
    }

    public List<QuizAttempt> findByUsername(String username) {
        return quizAttemptRepository.findByUser_UsernameIgnoreCaseWithQuiz(username);
    }

    public Optional<QuizAttempt> findByQuizIdAndUsername(Long quizId, String username) {
        return quizAttemptRepository.findByQuizIdAndUsername(quizId, username);
    }

    public Optional<QuizAttempt> findByIdWithQuiz(Long id) {
        return quizAttemptRepository.findByIdWithQuiz(id);
    }

    public void deleteById(Long id) {
        quizAttemptRepository.deleteById(id);
    }
}
