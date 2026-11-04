package sn.smtg.authservice.dto;

public record PasswordResetTokenCompletionRequest(String tokenHash, String motDePasse) {
}
