package sn.smtg.userservice.dto;

import java.time.LocalDateTime;

public record PasswordResetTokenRequest(String email, String tokenHash, LocalDateTime expiresAt) {
}
