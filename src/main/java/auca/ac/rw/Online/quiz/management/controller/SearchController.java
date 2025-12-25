package auca.ac.rw.Online.quiz.management.controller;

import auca.ac.rw.Online.quiz.management.model.EUserRole;
import auca.ac.rw.Online.quiz.management.model.Quiz;
import auca.ac.rw.Online.quiz.management.model.QuizAttempt;
import auca.ac.rw.Online.quiz.management.model.Question;
import auca.ac.rw.Online.quiz.management.model.User;
import auca.ac.rw.Online.quiz.management.repository.QuizAttemptRepository;
import auca.ac.rw.Online.quiz.management.repository.QuizRepository;
import auca.ac.rw.Online.quiz.management.repository.QuestionRepository;
import auca.ac.rw.Online.quiz.management.repository.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public SearchController(
            UserRepository userRepository,
            QuizRepository quizRepository,
            QuestionRepository questionRepository,
            QuizAttemptRepository quizAttemptRepository) {
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
        this.questionRepository = questionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    @GetMapping("/global")
    public ResponseEntity<Map<String, Object>> global(@RequestParam("q") String query) {
        String q = query == null ? "" : query.trim();
        
        // Get the current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = false;
        
        if (auth != null && auth.isAuthenticated()) {
            try {
                String usernameOrEmail = auth.getName();
                // Try to find user by username first, then by email (same as UserDetailsServiceImpl)
                User currentUser = userRepository.findByUsernameIgnoreCase(usernameOrEmail)
                    .orElse(userRepository.findByEmailIgnoreCase(usernameOrEmail).orElse(null));
                
                if (currentUser != null && currentUser.getRole() == EUserRole.ADMIN) {
                    isAdmin = true;
                }
            } catch (Exception e) {
                // If we can't determine the user, default to non-admin (more secure)
                isAdmin = false;
            }
        }
        
        // Limit results for each category (top 5-10 results)
        Pageable limit = Pageable.ofSize(10);
        
        List<User> users = q.isEmpty() 
            ? userRepository.findAll(limit).getContent()
            : userRepository.findByUsernameIgnoreCaseContaining(q, limit).getContent();
        
        // Filter out admin users if the current user is not an admin
        if (!isAdmin) {
            users = users.stream()
                .filter(user -> user.getRole() != EUserRole.ADMIN)
                .collect(Collectors.toList());
        }
        
        List<Quiz> quizzes = q.isEmpty()
            ? quizRepository.findAll(limit).getContent()
            : quizRepository.findByTitleContainingIgnoreCase(q, limit).getContent();
        
        List<Question> questions = q.isEmpty()
            ? questionRepository.findAll(limit).getContent()
            : questionRepository.findByTextContainingIgnoreCase(q, limit).getContent();
        
        List<QuizAttempt> attempts = q.isEmpty()
            ? quizAttemptRepository.findAll(limit).getContent()
            : quizAttemptRepository.searchAttempts(q, limit).getContent();

        // Remove sensitive data
        users.forEach(u -> u.setPassword(null));

        Map<String, Object> payload = new HashMap<>();
        payload.put("users", users);
        payload.put("quizzes", quizzes);
        payload.put("questions", questions);
        payload.put("attempts", attempts);
        payload.put("total", users.size() + quizzes.size() + questions.size() + attempts.size());
        
        return ResponseEntity.ok(payload);
    }
}

