package auca.ac.rw.Online.quiz.management.service;

import org.springframework.stereotype.Service;

@Service
public class GradingService {
    public double gradePercentage(int correct, int total) {
        if (total <= 0) {
            return 0.0;
        }
        return (correct * 100.0) / total;
    }
}


