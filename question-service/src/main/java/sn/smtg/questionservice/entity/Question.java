package sn.smtg.questionservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String questionTitle;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String reponseCorrecte;
    private String difficulte;
    private String categorie;
}
