package auca.ac.rw.Online.quiz.management.repository;

import auca.ac.rw.Online.quiz.management.model.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {
    List<UserAnswer> findByAttempt_Id(Long attemptId);
    List<UserAnswer> findByQuestion_Id(Long questionId);
}