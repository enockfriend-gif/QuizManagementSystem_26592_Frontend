package auca.ac.rw.Online.quiz.management.service;

import auca.ac.rw.Online.quiz.management.model.Quiz;
import auca.ac.rw.Online.quiz.management.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuizService {
    private final QuizRepository quizRepository;

    public QuizService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    public List<Quiz> findAll() { return quizRepository.findAll(); }
    public Optional<Quiz> findById(Long id) { return quizRepository.findById(id); }
    public Quiz save(Quiz quiz) { return quizRepository.save(quiz); }
    public void deleteById(Long id) { quizRepository.deleteById(id); }
}


