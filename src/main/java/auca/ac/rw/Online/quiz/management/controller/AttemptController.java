package auca.ac.rw.Online.quiz.management.controller;

import auca.ac.rw.Online.quiz.management.model.QuizAttempt;
import auca.ac.rw.Online.quiz.management.service.ReportService;
import auca.ac.rw.Online.quiz.management.service.QuizAttemptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/attempts")
public class AttemptController {

    private final QuizAttemptService quizAttemptService;
    private final ReportService reportService;

    public AttemptController(QuizAttemptService quizAttemptService, ReportService reportService) {
        this.quizAttemptService = quizAttemptService;
        this.reportService = reportService;
    }

    @GetMapping
    public List<QuizAttempt> list() { return quizAttemptService.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<QuizAttempt> get(@PathVariable Long id) {
        return quizAttemptService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/quiz/{quizId}/average-score")
    public ResponseEntity<Double> averageScore(@PathVariable Long quizId) {
        Double avg = reportService.scoreStatsByQuiz().getOrDefault(quizId, new java.util.DoubleSummaryStatistics()).getAverage();
        return ResponseEntity.ok(Double.isNaN(avg) ? 0.0 : avg);
    }

    @PostMapping
    public ResponseEntity<QuizAttempt> create(@RequestBody QuizAttempt attempt) {
        QuizAttempt saved = quizAttemptService.save(attempt);
        return ResponseEntity.created(URI.create("/api/attempts/" + saved.getId())).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quizAttemptService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuizAttempt> update(@PathVariable Long id, @RequestBody QuizAttempt attempt) {
        return quizAttemptService.findById(id)
                .map(existing -> {
                    existing.setUser(attempt.getUser());
                    existing.setQuiz(attempt.getQuiz());
                    existing.setSubmittedAt(attempt.getSubmittedAt());
                    existing.setScore(attempt.getScore());
                    existing.setStatus(attempt.getStatus());
                    QuizAttempt saved = quizAttemptService.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}


