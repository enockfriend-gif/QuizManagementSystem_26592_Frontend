package auca.ac.rw.Online.quiz.management.repository;

import auca.ac.rw.Online.quiz.management.model.OtpToken;
import auca.ac.rw.Online.quiz.management.model.OtpType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpTokenRepository extends JpaRepository<OtpToken, Long> {
    Optional<OtpToken> findFirstByEmailAndTypeAndUsedFalseAndExpiresAtAfterOrderByExpiresAtDesc(
            String email, OtpType type, LocalDateTime now);
}

