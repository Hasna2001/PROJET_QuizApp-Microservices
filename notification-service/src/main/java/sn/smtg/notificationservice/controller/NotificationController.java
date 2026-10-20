package sn.smtg.notificationservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.smtg.notificationservice.dto.NotificationRequest;
import sn.smtg.notificationservice.service.NotificationService;

@RestController
@RequestMapping("notification")
public class NotificationController {

    @Autowired
    NotificationService notificationService;

    @PostMapping("quiz-complete")
    public ResponseEntity<String> envoyerNotification(@RequestBody NotificationRequest request) {
        return notificationService.envoyerNotification(request);
    }
}
