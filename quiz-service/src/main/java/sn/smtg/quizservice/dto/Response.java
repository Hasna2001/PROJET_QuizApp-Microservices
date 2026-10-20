package sn.smtg.quizservice.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Response {
    private Integer id;          // identifiant de la QUESTION
    private String response;
}
