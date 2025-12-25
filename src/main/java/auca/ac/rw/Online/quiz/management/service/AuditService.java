package auca.ac.rw.Online.quiz.management.service;

import auca.ac.rw.Online.quiz.management.model.AuditLog;
import auca.ac.rw.Online.quiz.management.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.OffsetDateTime;

@Service
public class AuditService {
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    private void saveLog(String username, String action, String details, String type) {
        AuditLog log = new AuditLog();
        log.setTimestamp(OffsetDateTime.now());
        log.setUsername(username);
        log.setAction(action);
        log.setDetails(details);
        log.setType(type);
        auditLogRepository.save(log);

        auditLogger.info("{} | {} | {} | {} | {}",
                type, log.getTimestamp(), username, action, details);
    }

    public void logUserAction(String username, String action, String details) {
        saveLog(username, action, details, "USER_ACTION");
    }

    public void logQuizAction(String username, String quizTitle, String action) {
        saveLog(username, action, quizTitle, "QUIZ_ACTION");
    }

    public void logSystemAction(String action, String details) {
        saveLog("SYSTEM", action, details, "SYSTEM_ACTION");
    }

    public void logSecurityEvent(String username, String event, String details) {
        saveLog(username, event, details, "SECURITY_EVENT");
    }
}