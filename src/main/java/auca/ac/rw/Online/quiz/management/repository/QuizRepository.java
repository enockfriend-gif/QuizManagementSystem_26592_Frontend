package auca.ac.rw.Online.quiz.management.repository;

import auca.ac.rw.Online.quiz.management.model.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;


@Repository
public interface QuizRepository extends JpaRepository<Quiz, Long> {
    Page<Quiz> findByTitleContainingIgnoreCase(String title, Pageable pageable);
    List<Quiz> findByStatusAndStartTimeBefore(auca.ac.rw.Online.quiz.management.model.EQuizStatus status, java.time.OffsetDateTime time);
    List<Quiz> findByStatusAndEndTimeBefore(auca.ac.rw.Online.quiz.management.model.EQuizStatus status, java.time.OffsetDateTime time);
    List<Quiz> findByStatus(auca.ac.rw.Online.quiz.management.model.EQuizStatus status);
    
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.createdBy")
    List<Quiz> findAllWithCreatedBy();
    
    @Query("SELECT DISTINCT q FROM Quiz q LEFT JOIN FETCH q.createdBy WHERE q.status = :status")
    List<Quiz> findByStatusWithCreatedBy(auca.ac.rw.Online.quiz.management.model.EQuizStatus status);
}


