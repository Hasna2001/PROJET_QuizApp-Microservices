package sn.smtg.questionservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

// Réutilisé comme DTO de soumission : id porte l'identifiant de la QUESTION
@Data
@NoArgsConstructor
public class Response {
    private Integer id;
    private String response;
}
