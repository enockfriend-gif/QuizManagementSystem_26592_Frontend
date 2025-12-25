package auca.ac.rw.Online.quiz.management.service;

import auca.ac.rw.Online.quiz.management.model.Quiz;
import auca.ac.rw.Online.quiz.management.model.Question;
import auca.ac.rw.Online.quiz.management.model.QuizAttempt;
import auca.ac.rw.Online.quiz.management.model.User;
import auca.ac.rw.Online.quiz.management.repository.QuizRepository;
import auca.ac.rw.Online.quiz.management.repository.QuestionRepository;
import auca.ac.rw.Online.quiz.management.repository.QuizAttemptRepository;
import auca.ac.rw.Online.quiz.management.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;
    private final GradingService gradingService;
    private final QuestionRandomizationService questionRandomizationService;
    private final AuditService auditService;
    
    @PersistenceContext
    private EntityManager entityManager;

    public QuizService(QuizRepository quizRepository, QuestionRepository questionRepository,
            QuizAttemptRepository quizAttemptRepository, UserRepository userRepository,
            GradingService gradingService,
            QuestionRandomizationService questionRandomizationService, AuditService auditService) {
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
        this.userRepository = userRepository;
        this.gradingService = gradingService;
        this.questionRandomizationService = questionRandomizationService;
        this.auditService = auditService;
    }

    public List<Quiz> findAll() {
        // Use findAllWithCreatedBy to ensure createdBy is loaded for dashboard filtering
        return quizRepository.findAllWithCreatedBy();
    }

    public List<Quiz> findByStatus(auca.ac.rw.Online.quiz.management.model.EQuizStatus status) {
        // Use findByStatusWithCreatedBy to ensure createdBy is loaded
        return quizRepository.findByStatusWithCreatedBy(status);
    }

    public Optional<QuizAttempt> checkExistingAttempt(Long quizId, String username) {
        return quizAttemptRepository.findByQuizIdAndUsername(quizId, username);
    }

    public Optional<Quiz> findById(Long id) {
        return quizRepository.findById(id);
    }

    public Quiz save(Quiz quiz) {
        Quiz saved = quizRepository.save(quiz);
        auditService.logQuizAction("SYSTEM", quiz.getTitle(), "QUIZ_SAVED");
        return saved;
    }

    public void deleteById(Long id) {
        quizRepository.findById(id).ifPresent(quiz -> {
            auditService.logQuizAction("SYSTEM", quiz.getTitle(), "QUIZ_DELETED");
        });
        quizRepository.deleteById(id);
    }

    public Page<Quiz> search(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return quizRepository.findAll(pageable);
        }
        return quizRepository.findByTitleContainingIgnoreCase(q, pageable);
    }

    public List<Question> getQuizQuestions(Long quizId) {
        List<Question> questions = questionRepository.findByQuizId(quizId);
        return questionRandomizationService.randomizeQuizQuestions(questions, true, true);
    }

    public List<Question> getQuizQuestionsOrdered(Long quizId) {
        return questionRepository.findByQuizId(quizId);
    }

    @Transactional
    public QuizAttempt submitQuiz(Long quizId, Map<String, Object> submission, String username) {
        System.out.println("[QuizService] ========== STARTING QUIZ SUBMISSION ==========");
        System.out.println("[QuizService] Quiz ID: " + quizId + ", Username: " + username);
        
        // Validate inputs
        if (username == null || username.trim().isEmpty()) {
            throw new RuntimeException("Username cannot be null or empty");
        }
        if (quizId == null) {
            throw new RuntimeException("Quiz ID cannot be null");
        }
        
        // Check if user has already attempted this quiz
        Optional<QuizAttempt> existingAttempt = quizAttemptRepository.findByQuizIdAndUsername(quizId, username);
        if (existingAttempt.isPresent()) {
            throw new RuntimeException("You have already attempted this quiz. Each quiz can only be taken once.");
        }
        
        // Step 1: Get user - try username first, then email
        User foundUser = userRepository.findByUsernameIgnoreCase(username.trim())
            .orElseGet(() -> {
                System.out.println("[QuizService] User not found by username, trying email: " + username);
                return userRepository.findByEmailIgnoreCase(username.trim())
                    .orElseThrow(() -> new RuntimeException("User not found by username or email: " + username));
            });
        
        if (foundUser == null) {
            throw new RuntimeException("User lookup returned null for: " + username);
        }
        
        if (foundUser.getId() == null) {
            throw new RuntimeException("User ID is null for user: " + username);
        }
        
        Long userId = foundUser.getId();
        System.out.println("[QuizService] User found: ID=" + userId + ", username=" + foundUser.getUsername());
        
        // Step 2: Get managed entities - try EntityManager.find() first, fallback to repository
        User user = entityManager.find(User.class, userId);
        if (user == null) {
            System.out.println("[QuizService] EntityManager.find() returned null, using repository...");
            user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found in database with ID: " + userId));
            // Merge to ensure it's in persistence context
            user = entityManager.merge(user);
            System.out.println("[QuizService] User loaded via repository and merged");
        } else {
            System.out.println("[QuizService] User loaded via EntityManager.find()");
        }
        
        // Verify user is valid
        if (user == null || user.getId() == null) {
            throw new RuntimeException("User entity is null or has null ID after loading");
        }
        
        Quiz quiz = entityManager.find(Quiz.class, quizId);
        if (quiz == null) {
            System.out.println("[QuizService] EntityManager.find() returned null for quiz, using repository...");
            quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found in database with ID: " + quizId));
            quiz = entityManager.merge(quiz);
            System.out.println("[QuizService] Quiz loaded via repository and merged");
        } else {
            System.out.println("[QuizService] Quiz loaded via EntityManager.find()");
        }
        
        if (quiz == null || quiz.getId() == null) {
            throw new RuntimeException("Quiz entity is null or has null ID after loading");
        }
        
        System.out.println("[QuizService] Managed entities verified - User: " + user.getUsername() + 
                          " (ID: " + user.getId() + "), Quiz: " + quiz.getTitle() + " (ID: " + quiz.getId() + ")");
        
        // Step 3: Verify entities are in persistence context
        if (!entityManager.contains(user)) {
            System.out.println("[QuizService] User not in persistence context, merging...");
            user = entityManager.merge(user);
        }
        if (!entityManager.contains(quiz)) {
            System.out.println("[QuizService] Quiz not in persistence context, merging...");
            quiz = entityManager.merge(quiz);
        }
        
        // Step 4: Create the attempt
        QuizAttempt attempt = new QuizAttempt();
        System.out.println("[QuizService] Created new QuizAttempt instance");
        
        // Step 5: Set the managed entities
        attempt.setQuiz(quiz);
        System.out.println("[QuizService] Quiz set on attempt: " + attempt.getQuiz().getId());
        
        attempt.setUser(user);
        System.out.println("[QuizService] User set on attempt: " + (attempt.getUser() != null ? attempt.getUser().getUsername() : "NULL"));
        
        attempt.setStartedAt(java.time.OffsetDateTime.now());
        attempt.setSubmittedAt(java.time.OffsetDateTime.now());
        attempt.setStatus(auca.ac.rw.Online.quiz.management.model.EAttemptStatus.SUBMITTED);
        
        // Step 6: CRITICAL VERIFICATION - Check user is set
        User attemptUser = attempt.getUser();
        if (attemptUser == null) {
            System.err.println("[QuizService] ========== CRITICAL ERROR ==========");
            System.err.println("[QuizService] User is NULL on QuizAttempt after setUser()!");
            System.err.println("[QuizService] Original user variable: " + (user != null ? user.getUsername() + " (ID: " + user.getId() + ")" : "NULL"));
            System.err.println("[QuizService] User in persistence context: " + entityManager.contains(user));
            System.err.println("[QuizService] Quiz in persistence context: " + entityManager.contains(quiz));
            throw new RuntimeException("User is null on QuizAttempt - cannot save. This should never happen!");
        }
        
        if (attemptUser.getId() == null) {
            System.err.println("[QuizService] CRITICAL: User ID is null on QuizAttempt!");
            throw new RuntimeException("User ID is null on QuizAttempt - cannot save");
        }
        
        System.out.println("[QuizService] Attempt prepared - User: " + attemptUser.getUsername() + 
                          " (ID: " + attemptUser.getId() + "), Quiz: " + attempt.getQuiz().getTitle());
        
        // Step 7: Save the attempt
        try {
            System.out.println("[QuizService] Attempting to save QuizAttempt...");
            System.out.println("[QuizService] Attempt user before save: " + attempt.getUser().getUsername() + " (ID: " + attempt.getUser().getId() + ")");
            
            attempt = quizAttemptRepository.save(attempt);
            System.out.println("[QuizService] Attempt saved successfully with ID: " + attempt.getId());
            
            // Verify user is still set after save
            if (attempt.getUser() == null) {
                System.err.println("[QuizService] WARNING: User became null after save!");
            } else {
                System.out.println("[QuizService] User confirmed after save: " + attempt.getUser().getUsername());
            }
        } catch (jakarta.persistence.PersistenceException e) {
            System.err.println("[QuizService] ========== PERSISTENCE EXCEPTION ==========");
            System.err.println("[QuizService] Message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("[QuizService] Root cause: " + e.getCause().getMessage());
                System.err.println("[QuizService] Root cause class: " + e.getCause().getClass().getName());
            }
            System.err.println("[QuizService] Attempt user was: " + (attempt.getUser() != null ? attempt.getUser().getUsername() : "NULL"));
            e.printStackTrace();
            throw new RuntimeException("Failed to save quiz attempt: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("[QuizService] ========== GENERAL EXCEPTION ==========");
            System.err.println("[QuizService] Error: " + e.getMessage());
            System.err.println("[QuizService] Class: " + e.getClass().getName());
            e.printStackTrace();
            throw e;
        }

        // Step 8: Grade the attempt
        Map<String, Object> answers = (Map<String, Object>) submission.get("answers");
        if (answers != null && !answers.isEmpty()) {
            System.out.println("[QuizService] Grading attempt with " + answers.size() + " answers");
            attempt = gradingService.gradeAttempt(attempt, answers);
            System.out.println("[QuizService] Grading complete - Score: " + attempt.getScore());
        } else {
            System.out.println("[QuizService] No answers provided, skipping grading");
        }

        System.out.println("[QuizService] ========== QUIZ SUBMISSION COMPLETE ==========");
        return attempt;
    }
}
