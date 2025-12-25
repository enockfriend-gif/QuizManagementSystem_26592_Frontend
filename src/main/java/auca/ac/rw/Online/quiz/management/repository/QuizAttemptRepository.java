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

    List<QuizAttempt> findByUser_UsernameIgnoreCase(String username);
    
    @Query("SELECT a FROM QuizAttempt a JOIN FETCH a.quiz WHERE LOWER(a.user.username) = LOWER(:username)")
    List<QuizAttempt> findByUser_UsernameIgnoreCaseWithQuiz(@Param("username") String username);

    @Query("select avg(a.score) from QuizAttempt a where a.quiz.id = :quizId and a.score is not null")
    Double averageScoreByQuiz(@Param("quizId") Long quizId);
    
    @Query("SELECT a FROM QuizAttempt a WHERE " +
           "LOWER(a.user.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(a.quiz.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    org.springframework.data.domain.Page<QuizAttempt> searchAttempts(@Param("query") String query, org.springframework.data.domain.Pageable pageable);
    
    @Query("SELECT a FROM QuizAttempt a JOIN FETCH a.user WHERE a.quiz.id = :quizId AND LOWER(a.user.username) = LOWER(:username)")
    java.util.Optional<QuizAttempt> findByQuizIdAndUsername(@Param("quizId") Long quizId, @Param("username") String username);
    
    @Query("SELECT a FROM QuizAttempt a JOIN FETCH a.quiz JOIN FETCH a.user WHERE a.id = :id")
    java.util.Optional<QuizAttempt> findByIdWithQuiz(@Param("id") Long id);
    
    @Query("SELECT a FROM QuizAttempt a JOIN FETCH a.user JOIN FETCH a.quiz")
    List<QuizAttempt> findAllWithUserAndQuiz();
}
