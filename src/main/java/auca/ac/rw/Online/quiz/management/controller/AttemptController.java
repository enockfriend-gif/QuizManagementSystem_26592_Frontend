package auca.ac.rw.Online.quiz.management.controller;

import auca.ac.rw.Online.quiz.management.model.EUserRole;
import auca.ac.rw.Online.quiz.management.model.QuizAttempt;
import auca.ac.rw.Online.quiz.management.model.User;
import auca.ac.rw.Online.quiz.management.repository.UserAnswerRepository;
import auca.ac.rw.Online.quiz.management.repository.UserRepository;
import auca.ac.rw.Online.quiz.management.service.ReportService;
import auca.ac.rw.Online.quiz.management.service.QuizAttemptService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/attempts")
public class AttemptController {

    private final QuizAttemptService quizAttemptService;
    private final ReportService reportService;
    private final UserAnswerRepository userAnswerRepository;
    private final UserRepository userRepository;

    public AttemptController(QuizAttemptService quizAttemptService, ReportService reportService,
            UserAnswerRepository userAnswerRepository, UserRepository userRepository) {
        this.quizAttemptService = quizAttemptService;
        this.reportService = reportService;
        this.userAnswerRepository = userAnswerRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<QuizAttempt> list() {
        return quizAttemptService.findAll();
    }

    @GetMapping("/page")
    public Page<QuizAttempt> page(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return quizAttemptService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuizAttempt> get(@PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();
        User currentUser = userRepository.findByUsernameIgnoreCase(username)
                .orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        final User finalUser = currentUser;
        // Use findByIdWithQuiz to ensure quiz and user are loaded
        return quizAttemptService.findByIdWithQuiz(id)
                .map(attempt -> {
                    // Check authorization: Students can only view their own attempts
                    // Instructors can view attempts for quizzes they created
                    // Admins can view all attempts
                    if (finalUser.getRole() == EUserRole.STUDENT) {
                        // For students, allow viewing their own attempts
                        if (attempt.getUser() == null || attempt.getUser().getId() == null) {
                            System.err.println("[AttemptController] Attempt user is null or has null ID");
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).<QuizAttempt>build();
                        }
                        if (!attempt.getUser().getId().equals(finalUser.getId())) {
                            System.out.println("[AttemptController] Student " + finalUser.getUsername() + 
                                " (ID: " + finalUser.getId() + ") tried to access attempt by user " + 
                                (attempt.getUser() != null ? attempt.getUser().getUsername() + " (ID: " + attempt.getUser().getId() + ")" : "null"));
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).<QuizAttempt>build();
                        }
                        // Student owns this attempt - allow access
                        System.out.println("[AttemptController] Student " + finalUser.getUsername() + " authorized to view their own attempt");
                    } else if (finalUser.getRole() == EUserRole.INSTRUCTOR) {
                        if (attempt.getQuiz() == null || attempt.getQuiz().getCreatedBy() == null ||
                            !attempt.getQuiz().getCreatedBy().getId().equals(finalUser.getId())) {
                            // Instructors can only view attempts for their own quizzes
                            return ResponseEntity.status(HttpStatus.FORBIDDEN).<QuizAttempt>build();
                        }
                    }
                    // ADMIN can view all attempts
                    
                    // Ensure quiz is loaded and has an ID
                    if (attempt.getQuiz() == null) {
                        System.err.println("[AttemptController] ERROR: Quiz is null for attempt " + attempt.getId());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .<QuizAttempt>build();
                    }
                    
                    if (attempt.getQuiz().getId() == null) {
                        System.err.println("[AttemptController] ERROR: Quiz ID is null for attempt " + attempt.getId());
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .<QuizAttempt>build();
                    }
                    
                    // Force initialization of quiz to ensure it's fully loaded
                    Long quizId = attempt.getQuiz().getId();
                    System.out.println("[AttemptController] Returning attempt " + attempt.getId() + 
                        " with quiz ID: " + quizId + 
                        ", quizId property: " + attempt.getQuizId() +
                        ", quiz object: " + (attempt.getQuiz() != null ? "present" : "null"));
                    
                    return ResponseEntity.ok(attempt);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/answers")
    public ResponseEntity<List<auca.ac.rw.Online.quiz.management.model.UserAnswer>> getAttemptAnswers(
            @PathVariable Long id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String username = auth.getName();
        User currentUser = userRepository.findByUsernameIgnoreCase(username)
                .orElse(null);

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Check if attempt exists and user has permission - use findByIdWithQuiz to ensure quiz is loaded
        QuizAttempt attempt = quizAttemptService.findByIdWithQuiz(id).orElse(null);
        if (attempt == null) {
            return ResponseEntity.notFound().build();
        }

        // Same authorization logic as get method
        if (currentUser.getRole() == EUserRole.STUDENT) {
            if (attempt.getUser() == null || attempt.getUser().getId() == null) {
                System.err.println("[AttemptController] Attempt user is null or has null ID for answers");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (!attempt.getUser().getId().equals(currentUser.getId())) {
                System.out.println("[AttemptController] Student " + currentUser.getUsername() + 
                    " tried to access answers for attempt by user " + 
                    (attempt.getUser() != null ? attempt.getUser().getUsername() : "null"));
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else if (currentUser.getRole() == EUserRole.INSTRUCTOR) {
            if (attempt.getQuiz() == null || attempt.getQuiz().getCreatedBy() == null ||
                !attempt.getQuiz().getCreatedBy().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        // ADMIN can view all

        List<auca.ac.rw.Online.quiz.management.model.UserAnswer> answers = userAnswerRepository.findByAttempt_Id(id);
        return ResponseEntity.ok(answers);
    }

    @GetMapping("/my-attempts")
    public ResponseEntity<List<QuizAttempt>> getMyAttempts() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        String username = auth.getName();
        return ResponseEntity.ok(quizAttemptService.findByUsername(username));
    }

    @GetMapping("/quiz/{quizId}/average-score")
    public ResponseEntity<Double> averageScore(@PathVariable Long quizId) {
        Double avg = reportService.scoreStatsByQuiz().getOrDefault(quizId, new java.util.DoubleSummaryStatistics())
                .getAverage();
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
