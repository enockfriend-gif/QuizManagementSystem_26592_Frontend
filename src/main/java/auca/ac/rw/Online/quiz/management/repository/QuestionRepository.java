package auca.ac.rw.Online.quiz.management.repository;

import auca.ac.rw.Online.quiz.management.model.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface QuestionRepository extends JpaRepository<Question, Long> {
    org.springframework.data.domain.Page<Question> findByTextContainingIgnoreCase(String text, org.springframework.data.domain.Pageable pageable);
    java.util.List<Question> findByQuizId(Long quizId);
}


