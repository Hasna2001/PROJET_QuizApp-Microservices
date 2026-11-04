package sn.smtg.notificationservice.dto;

public record PasswordResetEmailRequest(String email, String resetUrl) {
}
