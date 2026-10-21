package sn.smtg.quizservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.smtg.quizservice.dto.QuestionWrapper;
import sn.smtg.quizservice.dto.QuizDTO;
import sn.smtg.quizservice.dto.Response;
import sn.smtg.quizservice.service.QuizService;

import java.util.List;

@RestController
@RequestMapping("quiz")
public class QuizController {

    @Autowired
    QuizService quizService;

    @PostMapping("creer")
    public ResponseEntity<String> createQuiz(@RequestBody QuizDTO quizDTO) {
        return quizService.createQuiz(quizDTO.getCategorie(), quizDTO.getNumQuestions(), quizDTO.getTitre());
    }

    @GetMapping(value = "get/{id}", produces = "application/json;charset=UTF-8")
    public ResponseEntity<List<QuestionWrapper>> getQuizQuestions(@PathVariable Integer id) {
        return quizService.questionsQuiz(id);
    }

    @PostMapping("submit/{id}")
    public ResponseEntity<Integer> submitQuiz(@PathVariable Integer id,
                                              @RequestParam Integer utilisateurId,
                                              @RequestBody List<Response> response) {
        return quizService.calculerResultat(id, utilisateurId, response);
    }
}
