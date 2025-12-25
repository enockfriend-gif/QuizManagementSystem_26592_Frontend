package auca.ac.rw.Online.quiz.management.service;

import auca.ac.rw.Online.quiz.management.model.Question;
import auca.ac.rw.Online.quiz.management.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;

    public QuestionService(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public List<Question> findAll() { return questionRepository.findAll(); }
    public Optional<Question> findById(Long id) { return questionRepository.findById(id); }
    public Question save(Question question) { return questionRepository.save(question); }
    public void deleteById(Long id) { questionRepository.deleteById(id); }
    
    public org.springframework.data.domain.Page<Question> search(String q, org.springframework.data.domain.Pageable pageable) {
        if (q == null || q.isBlank()) {
            return questionRepository.findAll(pageable);
        }
        return questionRepository.findByTextContainingIgnoreCase(q, pageable);
    }
    
    public List<Question> findByQuizId(Long quizId) {
        return questionRepository.findByQuizId(quizId);
    }
}


