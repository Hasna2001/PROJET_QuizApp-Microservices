package sn.smtg.quizservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sn.smtg.quizservice.dto.QuestionWrapper;
import sn.smtg.quizservice.dto.Response;

import java.util.List;

@FeignClient(name = "QUESTION-SERVICE")
public interface QuizInterface {

    @GetMapping("question/generate")
    ResponseEntity<List<Integer>> getQuestionForQuiz(@RequestParam("nomCategorie") String nomCategorie,
                                                     @RequestParam("nombreDeQuestions") Integer nombreDeQuestions);

    @PostMapping("question/getQuestions")
    ResponseEntity<List<QuestionWrapper>> getQuestionsFromId(@RequestBody List<Integer> questionIds);

    @PostMapping("question/getScore")
    ResponseEntity<Integer> getScore(@RequestBody List<Response> responses);
}
