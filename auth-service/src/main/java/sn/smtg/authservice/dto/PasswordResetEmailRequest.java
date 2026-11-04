package sn.smtg.authservice.dto;

public record PasswordResetEmailRequest(String email, String resetUrl) {
}
