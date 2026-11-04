package sn.smtg.userservice.dto;

public record PasswordResetCompletionRequest(String tokenHash, String motDePasse) {
}
