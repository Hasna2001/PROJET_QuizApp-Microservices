package sn.smtg.quizservice.dto;

import lombok.Data;

@Data
public class QuizDTO {
    private String categorie;
    private int numQuestions;
    private String titre;
}
