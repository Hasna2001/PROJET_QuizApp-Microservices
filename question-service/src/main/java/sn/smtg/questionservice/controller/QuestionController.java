package sn.smtg.questionservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.smtg.questionservice.dto.QuestionWrapper;
import sn.smtg.questionservice.dto.Response;
import sn.smtg.questionservice.entity.Question;
import sn.smtg.questionservice.service.QuestionService;

import java.util.List;

@RestController
@RequestMapping("question")
public class QuestionController {

    @Autowired
    QuestionService questionService;

    @GetMapping(value = "allQuestions", produces = "application/json;charset=UTF-8")
    public ResponseEntity<List<Question>> getAllQuestion() {
        return questionService.allQuestion();
    }

    @GetMapping(value = "categorie/{categorie}", produces = "application/json;charset=UTF-8")
    public List<Question> getQuestionbyCategorie(@PathVariable String categorie) {
        return questionService.getQuestionByCategorie(categorie);
    }

    @PostMapping("ajouter")
    public String ajouterQuestion(@RequestBody Question question) {
        return questionService.addQuestion(question);
    }

    // --- Endpoints dédiés à quiz-service ---
    @GetMapping("generate")
    public ResponseEntity<List<Integer>> getQuestionsForQuiz(@RequestParam String nomCategorie,
                                                             @RequestParam Integer nombreDeQuestions) {
        return questionService.getQuestionForQuiz(nomCategorie, nombreDeQuestions);
    }

    @PostMapping(value = "getQuestions", produces = "application/json;charset=UTF-8")
    public ResponseEntity<List<QuestionWrapper>> getQuestionsFromId(@RequestBody List<Integer> questionIds) {
        return questionService.getQuestionsFromId(questionIds);
    }

    @PostMapping("getScore")
    public ResponseEntity<Integer> getScore(@RequestBody List<Response> responses) {
        return questionService.getScore(responses);
    }
}
