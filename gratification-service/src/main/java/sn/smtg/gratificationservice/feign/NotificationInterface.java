package sn.smtg.gratificationservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import sn.smtg.gratificationservice.dto.NotificationRequest;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationInterface {

    @PostMapping("notification/quiz-complete")
    ResponseEntity<String> envoyerNotification(@RequestBody NotificationRequest request);
}
