package auca.ac.rw.Online.quiz.management.controller;

import auca.ac.rw.Online.quiz.management.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/scores/csv")
    public ResponseEntity<byte[]> exportScoresCsv() {
        byte[] data = reportService.exportScoresCsv();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=quiz-scores.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }
}


