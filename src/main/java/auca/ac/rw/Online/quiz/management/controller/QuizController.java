package auca.ac.rw.Online.quiz.management.controller;

import auca.ac.rw.Online.quiz.management.model.Quiz;
import auca.ac.rw.Online.quiz.management.model.QuizAttempt;
import auca.ac.rw.Online.quiz.management.repository.UserRepository;
import auca.ac.rw.Online.quiz.management.service.QuizService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;
    private final UserRepository userRepository;

    public QuizController(QuizService quizService, UserRepository userRepository) {
        this.quizService = quizService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public List<Quiz> list(@RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty()) {
            try {
                auca.ac.rw.Online.quiz.management.model.EQuizStatus quizStatus = 
                    auca.ac.rw.Online.quiz.management.model.EQuizStatus.valueOf(status.toUpperCase());
                return quizService.findByStatus(quizStatus);
            } catch (IllegalArgumentException e) {
                // Invalid status, return all quizzes
                return quizService.findAll();
            }
        }
        return quizService.findAll();
    }
    
    @GetMapping("/{id}/questions")
    public ResponseEntity<?> getQuizQuestions(@PathVariable Long id) {
        // Check if user has already attempted this quiz
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            String username = auth.getName();
            if (username != null && !username.trim().isEmpty()) {
                try {
                    java.util.Optional<QuizAttempt> existingAttempt = quizService.checkExistingAttempt(id, username);
                    if (existingAttempt.isPresent()) {
                        return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
                            .body("You have already attempted this quiz. Each quiz can only be taken once.");
                    }
                } catch (Exception e) {
                    // If check fails, allow access (fail open for now, but log the error)
                    System.err.println("[QuizController] Error checking existing attempt: " + e.getMessage());
                }
            }
        }
        return ResponseEntity.ok(quizService.getQuizQuestions(id));
    }
    
    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitQuiz(@PathVariable Long id, @RequestBody Map<String, Object> submission) {
        try {
            System.out.println("[QuizController] ========== QUIZ SUBMISSION REQUEST ==========");
            System.out.println("[QuizController] Quiz ID: " + id);
            System.out.println("[QuizController] Submission data: " + submission);
            
            // Get the current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            System.out.println("[QuizController] Authentication object: " + (auth != null ? "EXISTS" : "NULL"));
            
            if (auth == null) {
                System.err.println("[QuizController] ERROR: Authentication is NULL - user not authenticated!");
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body("User not authenticated - no authentication found");
            }
            
            String username = auth.getName();
            System.out.println("[QuizController] Username from authentication: '" + username + "'");
            System.out.println("[QuizController] Username is null: " + (username == null));
            System.out.println("[QuizController] Username is empty: " + (username != null && username.isEmpty()));
            System.out.println("[QuizController] Username is blank: " + (username != null && username.isBlank()));
            
            if (username == null || username.trim().isEmpty()) {
                System.err.println("[QuizController] ERROR: Username is null or empty!");
                System.err.println("[QuizController] Auth principal: " + auth.getPrincipal());
                System.err.println("[QuizController] Auth details: " + auth.getDetails());
                return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body("User not authenticated - username is null or empty");
            }
            
            System.out.println("[QuizController] Proceeding with username: " + username);
            QuizAttempt attempt = quizService.submitQuiz(id, submission, username);
            System.out.println("[QuizController] Quiz submitted successfully, attempt ID: " + attempt.getId());
            return ResponseEntity.ok(attempt);
        } catch (Exception e) {
            System.err.println("[QuizController] ========== EXCEPTION IN CONTROLLER ==========");
            System.err.println("[QuizController] Error type: " + e.getClass().getName());
            System.err.println("[QuizController] Error message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("[QuizController] Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to submit quiz: " + e.getMessage());
        }
    }

    @GetMapping("/page")
    public Page<Quiz> page(@RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(defaultValue = "") String q) {
        Pageable pageable = PageRequest.of(page, size);
        return quizService.search(q, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        // Allow all authenticated users to view quiz details
        // The restriction on taking quizzes is handled in getQuizQuestions() and submitQuiz()
        // This allows students to view quiz details when viewing their results
        return quizService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Quiz> create(@RequestBody Quiz quiz) {
        Quiz saved = quizService.save(quiz);
        return ResponseEntity.created(URI.create("/api/quizzes/" + saved.getId())).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        quizService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Quiz> update(@PathVariable Long id, @RequestBody Quiz quiz) {
        return quizService.findById(id)
                .map(existing -> {
                    existing.setTitle(quiz.getTitle());
                    existing.setStatus(quiz.getStatus());
                    existing.setCreatedBy(quiz.getCreatedBy());
                    existing.setStartTime(quiz.getStartTime());
                    existing.setEndTime(quiz.getEndTime());
                    existing.setDurationMinutes(quiz.getDurationMinutes());
                    Quiz saved = quizService.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}


