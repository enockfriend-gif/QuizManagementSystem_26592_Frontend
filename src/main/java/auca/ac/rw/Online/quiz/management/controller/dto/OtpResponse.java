package auca.ac.rw.Online.quiz.management.controller.dto;

public record OtpResponse(String email, String message) {
    public static OtpResponse success(String email) {
        return new OtpResponse(email, "OTP has been sent to your email address");
    }
}

