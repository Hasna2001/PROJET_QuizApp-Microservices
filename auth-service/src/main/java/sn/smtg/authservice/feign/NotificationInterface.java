package sn.smtg.authservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import sn.smtg.authservice.dto.PasswordResetEmailRequest;

@FeignClient(name = "NOTIFICATION-SERVICE", url = "${notification.service.url:}")
public interface NotificationInterface {

    @PostMapping("notification/password-reset")
    ResponseEntity<String> envoyerLienReinitialisation(
            @RequestBody PasswordResetEmailRequest request,
            @RequestHeader("X-Internal-Service-Token") String serviceToken);
}
