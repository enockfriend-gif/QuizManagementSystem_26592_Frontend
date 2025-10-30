package auca.ac.rw.Online.quiz.management.repository;

import auca.ac.rw.Online.quiz.management.model.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
    List<QuizAttempt> findByQuiz_Id(Long quizId);

    @Query("select avg(a.score) from QuizAttempt a where a.quiz.id = :quizId and a.score is not null")
    Double averageScoreByQuiz(@Param("quizId") Long quizId);
}


