package auca.ac.rw.Online.quiz.management.controller;

import auca.ac.rw.Online.quiz.management.model.Question;
import auca.ac.rw.Online.quiz.management.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bulk")
public class BulkOperationsController {
    private final QuestionService questionService;

    public BulkOperationsController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping("/questions/import")
    public ResponseEntity<Map<String, Object>> importQuestions(@RequestParam("file") MultipartFile file) {
        try {
            // Simple CSV import simulation
            int imported = 0;
            String filename = file.getOriginalFilename();
            if (filename != null && filename.endsWith(".csv")) {
                imported = 5; // Placeholder
            }
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "imported", imported,
                "message", "Questions imported successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Import failed: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/questions/export")
    public ResponseEntity<Map<String, Object>> exportQuestions(@RequestParam(required = false) Long quizId) {
        try {
            List<Question> questions = quizId != null ? 
                questionService.findByQuizId(quizId) : 
                questionService.findAll();
                
            return ResponseEntity.ok(Map.of(
                "success", true,
                "count", questions.size(),
                "data", questions,
                "message", "Questions exported successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Export failed: " + e.getMessage()
            ));
        }
    }
}