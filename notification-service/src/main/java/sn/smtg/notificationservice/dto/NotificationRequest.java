package sn.smtg.notificationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private Integer utilisateurId;
    private Integer quizId;
    private Integer score;
    private Integer points;
}
