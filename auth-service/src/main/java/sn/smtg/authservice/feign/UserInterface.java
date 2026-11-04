package sn.smtg.authservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import sn.smtg.authservice.dto.PasswordResetTokenRequest;
import sn.smtg.authservice.dto.PasswordResetTokenCompletionRequest;
import sn.smtg.authservice.dto.UserWrapper;

@FeignClient(name = "USER-SERVICE")
public interface UserInterface {

    @GetMapping("user/internal/email/{email}")
    ResponseEntity<UserWrapper> getUserByEmail(
            @PathVariable("email") String email,
            @RequestHeader("X-Internal-Service-Token") String serviceToken);

    @PostMapping("user/internal/password-reset/token")
    ResponseEntity<String> enregistrerJetonReinitialisation(
            @RequestBody PasswordResetTokenRequest request,
            @RequestHeader("X-Internal-Service-Token") String serviceToken);

    @PostMapping("user/internal/password-reset/complete")
    ResponseEntity<String> terminerReinitialisation(
            @RequestBody PasswordResetTokenCompletionRequest request,
            @RequestHeader("X-Internal-Service-Token") String serviceToken);
}
