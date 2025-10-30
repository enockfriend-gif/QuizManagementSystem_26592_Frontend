package auca.ac.rw.Online.quiz.management.service;

import auca.ac.rw.Online.quiz.management.model.QuizAttempt;
import auca.ac.rw.Online.quiz.management.repository.QuizAttemptRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final QuizAttemptRepository quizAttemptRepository;

    public ReportService(QuizAttemptRepository quizAttemptRepository) {
        this.quizAttemptRepository = quizAttemptRepository;
    }

    public Map<Long, DoubleSummaryStatistics> scoreStatsByQuiz() {
        List<QuizAttempt> attempts = quizAttemptRepository.findAll();
        return attempts.stream()
                .filter(a -> a.getQuiz() != null && a.getScore() != null)
                .collect(Collectors.groupingBy(a -> a.getQuiz().getId(),
                        Collectors.summarizingDouble(QuizAttempt::getScore)));
    }

    public byte[] exportScoresCsv() {
        List<QuizAttempt> attempts = quizAttemptRepository.findAll();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.writeBytes("attempt_id,quiz_id,user_id,score,status\n".getBytes(StandardCharsets.UTF_8));
        for (QuizAttempt a : attempts) {
            String line = String.format("%d,%d,%d,%.2f,%s\n",
                    a.getId(),
                    a.getQuiz() != null ? a.getQuiz().getId() : null,
                    a.getUser() != null ? a.getUser().getId() : null,
                    a.getScore() != null ? a.getScore() : 0.0,
                    a.getStatus() != null ? a.getStatus().name() : "");
            out.writeBytes(line.getBytes(StandardCharsets.UTF_8));
        }
        return out.toByteArray();
    }
}


