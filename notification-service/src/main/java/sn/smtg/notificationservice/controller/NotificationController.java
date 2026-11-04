package sn.smtg.notificationservice.controller;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.smtg.notificationservice.dto.PasswordResetEmailRequest;
import sn.smtg.notificationservice.dto.NotificationRequest;
import sn.smtg.notificationservice.service.NotificationService;

@RestController
@RequestMapping("notification")
public class NotificationController {

    @Autowired
    NotificationService notificationService;

    @Value("${notification.internal-token:}")
    private String internalServiceToken;

    @PostMapping("quiz-complete")
    public ResponseEntity<String> envoyerNotification(@RequestBody NotificationRequest request) {
        return notificationService.envoyerNotification(request);
    }

    @PostMapping("password-reset")
    public ResponseEntity<String> envoyerLienReinitialisation(
            @RequestBody PasswordResetEmailRequest request,
            @RequestHeader(value = "X-Internal-Service-Token", required = false) String serviceToken) {
        if (internalServiceToken.isBlank() || serviceToken == null
                || !MessageDigest.isEqual(
                        internalServiceToken.getBytes(StandardCharsets.UTF_8),
                        serviceToken.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return notificationService.envoyerLienReinitialisation(request);
    }
}
