package sn.smtg.leaderboardservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GratificationWrapper {
    private Integer id;
    private Integer utilisateurId;
    private Integer quizId;
    private Integer score;
    private Integer points;
}
