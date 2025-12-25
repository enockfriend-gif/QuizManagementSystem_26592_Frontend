package auca.ac.rw.Online.quiz.management.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.transaction.TransactionSystemException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.CONFLICT.value());
        error.put("error", "Data Integrity Violation");
        
        String message = e.getMessage();
        if (e.getCause() != null) {
            message = e.getCause().getMessage();
        }
        
        // Check for specific constraint violations
        if (message != null) {
            if (message.contains("unique constraint") || message.contains("UNIQUE")) {
                error.put("message", "A record with this value already exists. Please use a different value.");
            } else if (message.contains("foreign key") || message.contains("FOREIGN KEY")) {
                error.put("message", "Cannot delete or update this record because it is referenced by other records.");
            } else if (message.contains("not-null") || message.contains("NOT NULL")) {
                error.put("message", "Required field is missing: " + message);
            } else {
                error.put("message", "Database constraint violation: " + message);
            }
        } else {
            error.put("message", "Data integrity violation occurred");
        }
        
        System.err.println("[GlobalExceptionHandler] DataIntegrityViolationException: " + message);
        e.printStackTrace();
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, Object>> handleTransactionException(TransactionSystemException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Transaction Error");
        
        String message = e.getMessage();
        Throwable rootCause = e.getRootCause();
        if (rootCause != null) {
            message = rootCause.getMessage();
        }
        
        error.put("message", "Transaction failed: " + (message != null ? message : e.getMessage()));
        
        System.err.println("[GlobalExceptionHandler] TransactionSystemException: " + message);
        e.printStackTrace();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException e) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.BAD_REQUEST.value());
        error.put("error", "Invalid Argument");
        error.put("message", e.getMessage());
        
        System.err.println("[GlobalExceptionHandler] IllegalArgumentException: " + e.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        Map<String, Object> error = new HashMap<>();
        error.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.put("error", "Internal Server Error");
        error.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred");
        
        if (e.getCause() != null) {
            error.put("cause", e.getCause().getMessage());
        }
        
        System.err.println("[GlobalExceptionHandler] Exception: " + e.getClass().getName());
        System.err.println("[GlobalExceptionHandler] Message: " + e.getMessage());
        if (e.getCause() != null) {
            System.err.println("[GlobalExceptionHandler] Cause: " + e.getCause().getMessage());
        }
        e.printStackTrace();
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

