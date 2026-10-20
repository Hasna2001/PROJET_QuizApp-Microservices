package sn.smtg.leaderboardservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassementEntry {
    private Integer rang;
    private Integer utilisateurId;
    private String nom;
    private String prenom;
    private String email;
    private Integer totalPoints;
    private Integer nombreQuiz;
}
