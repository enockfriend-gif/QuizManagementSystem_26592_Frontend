package auca.ac.rw.Online.quiz.management.controller;

import auca.ac.rw.Online.quiz.management.model.Question;
import auca.ac.rw.Online.quiz.management.model.Quiz;
import auca.ac.rw.Online.quiz.management.model.Option;
import auca.ac.rw.Online.quiz.management.service.QuestionService;
import auca.ac.rw.Online.quiz.management.repository.QuizRepository;
import auca.ac.rw.Online.quiz.management.repository.OptionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final QuizRepository quizRepository;
    private final OptionRepository optionRepository;
    
    @PersistenceContext
    private EntityManager entityManager;

    public QuestionController(QuestionService questionService, QuizRepository quizRepository, OptionRepository optionRepository) {
        this.questionService = questionService;
        this.quizRepository = quizRepository;
        this.optionRepository = optionRepository;
    }

    @GetMapping
    public List<Question> list() { return questionService.findAll(); }
    
    @GetMapping("/page")
    public org.springframework.data.domain.Page<Question> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String q) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return questionService.search(q, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Question> get(@PathVariable Long id) {
        return questionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Transactional
    public ResponseEntity<?> create(@RequestBody Map<String, Object> payload) {
        try {
            System.out.println("[QuestionController] ========== STARTING QUESTION CREATION ==========");
            System.out.println("[QuestionController] Creating question with payload: " + payload);
            
            // Verify optionRepository is available
            if (optionRepository == null) {
                System.err.println("[QuestionController] CRITICAL: OptionRepository is null!");
                return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("OptionRepository is not available");
            }
            System.out.println("[QuestionController] OptionRepository is available");
            
            // Extract quizId from payload
            Object quizIdObj = payload.get("quizId");
            if (quizIdObj == null) {
                return ResponseEntity.badRequest().body("quizId is required");
            }
            
            Long quizId;
            if (quizIdObj instanceof Number) {
                quizId = ((Number) quizIdObj).longValue();
            } else if (quizIdObj instanceof String) {
                quizId = Long.parseLong((String) quizIdObj);
            } else {
                return ResponseEntity.badRequest().body("Invalid quizId format");
            }
            
            System.out.println("[QuestionController] Quiz ID: " + quizId);
            
            // Load the Quiz entity - ensure it's managed
            Quiz quiz = entityManager.find(Quiz.class, quizId);
            if (quiz == null) {
                quiz = quizRepository.findById(quizId)
                    .orElseThrow(() -> new RuntimeException("Quiz not found with ID: " + quizId));
                quiz = entityManager.merge(quiz);
            }
            
            System.out.println("[QuestionController] Quiz loaded: " + quiz.getTitle());
            
            // Create Question entity
            Question question = new Question();
            question.setText((String) payload.get("text"));
            
            // Parse question type safely
            String typeStr = payload.getOrDefault("type", "MULTIPLE_CHOICE").toString().toUpperCase();
            try {
                question.setType(auca.ac.rw.Online.quiz.management.model.EQuestionType.valueOf(typeStr));
            } catch (IllegalArgumentException e) {
                System.err.println("[QuestionController] Invalid question type: " + typeStr + ", defaulting to MULTIPLE_CHOICE");
                question.setType(auca.ac.rw.Online.quiz.management.model.EQuestionType.MULTIPLE_CHOICE);
            }
            
            Object pointsObj = payload.get("points");
            if (pointsObj != null) {
                try {
                    question.setPoints(pointsObj instanceof Number ? 
                        ((Number) pointsObj).intValue() : Integer.parseInt(pointsObj.toString()));
                } catch (NumberFormatException e) {
                    System.err.println("[QuestionController] Invalid points value, defaulting to 1");
                    question.setPoints(1);
                }
            } else {
                question.setPoints(1); // Default points
            }
            
            // Set the Quiz entity (required)
            question.setQuiz(quiz);
            
            // Verify quiz is set
            if (question.getQuiz() == null) {
                throw new RuntimeException("Quiz is null on Question before save");
            }
            
            System.out.println("[QuestionController] Question prepared - Quiz: " + question.getQuiz().getId());
            
            // Save the question first to get an ID
            question = questionService.save(question);
            Long questionId = question.getId();
            System.out.println("[QuestionController] Question saved with ID: " + questionId);
            
            // Flush to ensure question is persisted
            entityManager.flush();
            System.out.println("[QuestionController] Entity manager flushed");
            
            // Get managed question reference for options
            Question managedQuestion = entityManager.find(Question.class, questionId);
            if (managedQuestion == null) {
                throw new RuntimeException("Question not found after save with ID: " + questionId);
            }
            System.out.println("[QuestionController] Managed question retrieved - ID: " + managedQuestion.getId() + ", Quiz: " + managedQuestion.getQuiz().getId());
            
            // Handle options if provided
            Object optionsObj = payload.get("options");
            if (optionsObj instanceof List) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> optionsList = (List<Map<String, Object>>) optionsObj;
                
                int savedCount = 0;
                for (Map<String, Object> optionData : optionsList) {
                    String optionText = (String) optionData.get("text");
                    if (optionText != null && !optionText.trim().isEmpty()) {
                        try {
                            Option option = new Option();
                            option.setText(optionText.trim());
                            
                            Object isCorrectObj = optionData.get("isCorrect");
                            boolean isCorrect = isCorrectObj instanceof Boolean ? 
                                (Boolean) isCorrectObj : 
                                Boolean.parseBoolean(String.valueOf(isCorrectObj));
                            option.setCorrect(isCorrect);
                            
                            // Set the managed question
                            option.setQuestion(managedQuestion);
                            
                            // Verify question is set
                            if (option.getQuestion() == null || option.getQuestion().getId() == null) {
                                throw new RuntimeException("Question is null on Option before save");
                            }
                            
                            System.out.println("[QuestionController] Saving option: " + optionText + " for question ID: " + managedQuestion.getId());
                            
                            // Use entityManager to persist option
                            entityManager.persist(option);
                            entityManager.flush();
                            
                            savedCount++;
                            System.out.println("[QuestionController] Saved option: " + optionText + " (correct: " + isCorrect + ") with ID: " + option.getId());
                        } catch (Exception e) {
                            System.err.println("[QuestionController] Error saving option: " + optionText + " - " + e.getMessage());
                            e.printStackTrace();
                            throw new RuntimeException("Failed to save option: " + optionText + " - " + e.getMessage(), e);
                        }
                    }
                }
                System.out.println("[QuestionController] Saved " + savedCount + " options");
            } else if (managedQuestion.getType() == auca.ac.rw.Online.quiz.management.model.EQuestionType.TRUE_FALSE) {
                // For TRUE_FALSE questions, create default options if not provided
                System.out.println("[QuestionController] Creating default TRUE_FALSE options");
                
                Option trueOption = new Option();
                trueOption.setText("True");
                trueOption.setCorrect(false); // Default - instructor should set correct answer
                trueOption.setQuestion(managedQuestion);
                entityManager.persist(trueOption);
                
                Option falseOption = new Option();
                falseOption.setText("False");
                falseOption.setCorrect(false);
                falseOption.setQuestion(managedQuestion);
                entityManager.persist(falseOption);
                
                entityManager.flush();
                
                System.out.println("[QuestionController] Created default TRUE_FALSE options");
            }
            
            // Reload question with options for response
            Question savedQuestion = questionService.findById(question.getId())
                .orElse(question);
            
            return ResponseEntity.created(URI.create("/api/questions/" + savedQuestion.getId())).body(savedQuestion);
        } catch (jakarta.persistence.PersistenceException e) {
            System.err.println("[QuestionController] ========== PERSISTENCE EXCEPTION ==========");
            System.err.println("[QuestionController] Error: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("[QuestionController] Root cause: " + e.getCause().getMessage());
                System.err.println("[QuestionController] Root cause class: " + e.getCause().getClass().getName());
            }
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to create question: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        } catch (IllegalArgumentException e) {
            System.err.println("[QuestionController] IllegalArgumentException: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Invalid data: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("[QuestionController] ========== GENERAL EXCEPTION ==========");
            System.err.println("[QuestionController] Error type: " + e.getClass().getName());
            System.err.println("[QuestionController] Error message: " + e.getMessage());
            if (e.getCause() != null) {
                System.err.println("[QuestionController] Cause: " + e.getCause().getMessage());
            }
            e.printStackTrace();
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Failed to create question: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Map<String, Object> payload) {
        try {
            System.out.println("[QuestionController] Updating question ID: " + id);
            
            Question existing = questionService.findById(id)
                .orElseThrow(() -> new RuntimeException("Question not found with ID: " + id));
            
            // Update question fields
            if (payload.containsKey("text")) {
                existing.setText((String) payload.get("text"));
            }
            if (payload.containsKey("type")) {
                existing.setType(auca.ac.rw.Online.quiz.management.model.EQuestionType.valueOf(
                    payload.get("type").toString()));
            }
            if (payload.containsKey("points")) {
                Object pointsObj = payload.get("points");
                existing.setPoints(pointsObj instanceof Number ? 
                    ((Number) pointsObj).intValue() : Integer.parseInt(pointsObj.toString()));
            }
            
            // Handle quiz update if quizId is provided
            if (payload.containsKey("quizId")) {
                Object quizIdObj = payload.get("quizId");
                Long quizId = quizIdObj instanceof Number ? 
                    ((Number) quizIdObj).longValue() : Long.parseLong(quizIdObj.toString());
                
                Quiz quiz = entityManager.find(Quiz.class, quizId);
                if (quiz == null) {
                    quiz = quizRepository.findById(quizId)
                        .orElseThrow(() -> new RuntimeException("Quiz not found with ID: " + quizId));
                    quiz = entityManager.merge(quiz);
                }
                existing.setQuiz(quiz);
            }
            
            // Save the question
                    Question saved = questionService.save(existing);
            
            // Handle options update if provided
            if (payload.containsKey("options")) {
                // Delete existing options
                optionRepository.deleteByQuestionId(id);
                
                // Add new options
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> optionsList = (List<Map<String, Object>>) payload.get("options");
                
                for (Map<String, Object> optionData : optionsList) {
                    String optionText = (String) optionData.get("text");
                    if (optionText != null && !optionText.trim().isEmpty()) {
                        Option option = new Option();
                        option.setText(optionText.trim());
                        
                        Object isCorrectObj = optionData.get("isCorrect");
                        boolean isCorrect = isCorrectObj instanceof Boolean ? 
                            (Boolean) isCorrectObj : 
                            Boolean.parseBoolean(String.valueOf(isCorrectObj));
                        option.setCorrect(isCorrect);
                        
                        option.setQuestion(saved);
                        optionRepository.save(option);
                    }
                }
            }
            
                    return ResponseEntity.ok(saved);
        } catch (Exception e) {
            System.err.println("[QuestionController] Error updating question: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Failed to update question: " + e.getMessage());
        }
    }
}


