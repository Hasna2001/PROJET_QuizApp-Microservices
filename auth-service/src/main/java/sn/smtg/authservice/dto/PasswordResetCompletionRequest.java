package sn.smtg.authservice.dto;

public record PasswordResetCompletionRequest(String token, String motDePasse) {
}
