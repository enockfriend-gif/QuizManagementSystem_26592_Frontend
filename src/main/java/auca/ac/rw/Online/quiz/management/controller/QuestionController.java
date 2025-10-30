package auca.ac.rw.Online.quiz.management.controller;

import auca.ac.rw.Online.quiz.management.model.Question;
import auca.ac.rw.Online.quiz.management.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @GetMapping
    public List<Question> list() { return questionService.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Question> get(@PathVariable Long id) {
        return questionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Question> create(@RequestBody Question question) {
        Question saved = questionService.save(question);
        return ResponseEntity.created(URI.create("/api/questions/" + saved.getId())).body(saved);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        questionService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Question> update(@PathVariable Long id, @RequestBody Question question) {
        return questionService.findById(id)
                .map(existing -> {
                    existing.setText(question.getText());
                    existing.setType(question.getType());
                    existing.setOptions(question.getOptions());
                    existing.setQuiz(question.getQuiz());
                    Question saved = questionService.save(existing);
                    return ResponseEntity.ok(saved);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}


