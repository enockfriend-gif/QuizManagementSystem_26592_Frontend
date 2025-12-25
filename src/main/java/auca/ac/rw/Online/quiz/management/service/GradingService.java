package auca.ac.rw.Online.quiz.management.service;

import auca.ac.rw.Online.quiz.management.model.*;
import auca.ac.rw.Online.quiz.management.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class GradingService {
    private final UserAnswerRepository userAnswerRepository;
    private final QuestionRepository questionRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public GradingService(UserAnswerRepository userAnswerRepository,
            QuestionRepository questionRepository,
            QuizAttemptRepository quizAttemptRepository) {
        this.userAnswerRepository = userAnswerRepository;
        this.questionRepository = questionRepository;
        this.quizAttemptRepository = quizAttemptRepository;
    }

    @Transactional
    public QuizAttempt gradeAttempt(QuizAttempt attempt, Map<String, Object> answers) {
        // Ensure attempt has user before grading
        if (attempt.getUser() == null) {
            System.err.println("[GradingService] WARNING: Attempt user is null, cannot grade");
            throw new RuntimeException("QuizAttempt user is null - cannot grade attempt");
        }
        
        System.out.println("[GradingService] Grading attempt ID: " + attempt.getId() + " for user: " + attempt.getUser().getUsername());
        
        List<Question> questions = questionRepository.findByQuizId(attempt.getQuiz().getId());
        int totalPoints = 0;
        int earnedPoints = 0;

        for (Question question : questions) {
            totalPoints += question.getPoints() != null ? question.getPoints() : 1;

            UserAnswer userAnswer = new UserAnswer();
            userAnswer.setAttempt(attempt);
            userAnswer.setQuestion(question);

            String answerKey = question.getId().toString();
            if (answers.containsKey(answerKey)) {
                String userResponse = answers.get(answerKey).toString();
                
                // Handle different question types
                if (question.getType() == EQuestionType.TRUE_FALSE) {
                    // For TRUE_FALSE, find the option that matches the text
                    Long optionId = findOptionIdByText(question, userResponse);
                    if (optionId != null) {
                        userAnswer.setSelectedOptionId(optionId);
                    } else {
                        // If no matching option found, store as text answer
                        userAnswer.setTextAnswer(userResponse);
                    }
                } else {
                    // For MULTIPLE_CHOICE, try to parse as Long (option ID)
                    try {
                        Long optionId = Long.parseLong(userResponse);
                        userAnswer.setSelectedOptionId(optionId);
                    } catch (NumberFormatException e) {
                        // If not a number, store as text answer
                        userAnswer.setTextAnswer(userResponse);
                    }
                }

                boolean isCorrect = checkAnswer(question, userResponse);
                userAnswer.setIsCorrect(isCorrect);

                if (isCorrect) {
                    int points = question.getPoints() != null ? question.getPoints() : 1;
                    userAnswer.setPointsEarned(points);
                    earnedPoints += points;
                } else {
                    userAnswer.setPointsEarned(0);
                }
            } else {
                userAnswer.setIsCorrect(false);
                userAnswer.setPointsEarned(0);
            }

            userAnswerRepository.save(userAnswer);
        }

        int finalScore = totalPoints > 0 ? (earnedPoints * 100 / totalPoints) : 0;
        attempt.setScore((double) finalScore);
        attempt.setStatus(EAttemptStatus.GRADED);

        return quizAttemptRepository.save(attempt);
    }
    
    private Long findOptionIdByText(Question question, String text) {
        if (question.getOptions() == null || question.getOptions().isEmpty()) {
            return null;
        }
        return question.getOptions().stream()
            .filter(option -> option.getText() != null && 
                    option.getText().equalsIgnoreCase(text))
            .map(Option::getId)
            .findFirst()
            .orElse(null);
    }

    private boolean checkAnswer(Question question, String userResponse) {
        if (question.getType() == EQuestionType.MULTIPLE_CHOICE) {
            return question.getOptions().stream()
                    .anyMatch(option -> option.getIsCorrect() &&
                            option.getId().toString().equals(userResponse));
        }
        if (question.getType() == EQuestionType.TRUE_FALSE) {
            // Assume correct answer is stored in question or first correct option
            return question.getOptions().stream()
                    .anyMatch(option -> option.getIsCorrect() &&
                            option.getText().equalsIgnoreCase(userResponse));
        }
        return false;
    }
}