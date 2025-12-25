package auca.ac.rw.Online.quiz.management.service;

import auca.ac.rw.Online.quiz.management.model.Question;
import auca.ac.rw.Online.quiz.management.model.Option;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Collections;
import java.util.ArrayList;

@Service
public class QuestionRandomizationService {

    public List<Question> shuffleQuestions(List<Question> questions) {
        List<Question> shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    public Question shuffleOptions(Question question) {
        if (question.getOptions() != null && !question.getOptions().isEmpty()) {
            List<Option> shuffledOptions = new ArrayList<>(question.getOptions());
            Collections.shuffle(shuffledOptions);
            question.setOptions(shuffledOptions);
        }
        return question;
    }

    public List<Question> randomizeQuizQuestions(List<Question> questions, boolean shuffleQuestions, boolean shuffleOptions) {
        List<Question> result = new ArrayList<>(questions);
        
        if (shuffleQuestions) {
            result = shuffleQuestions(result);
        }
        
        if (shuffleOptions) {
            result = result.stream()
                .map(this::shuffleOptions)
                .toList();
        }
        
        return result;
    }
}