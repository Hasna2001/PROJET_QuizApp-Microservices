package sn.smtg.quizservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GratificationDTO {
    private Integer utilisateurId;
    private Integer quizId;
    private Integer score;
}
