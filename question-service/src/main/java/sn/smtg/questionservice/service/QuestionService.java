package sn.smtg.questionservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sn.smtg.questionservice.dto.QuestionWrapper;
import sn.smtg.questionservice.dto.Response;
import sn.smtg.questionservice.entity.Question;
import sn.smtg.questionservice.repository.QuestionRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class QuestionService {

    @Autowired
    QuestionRepo repo;

    public ResponseEntity<List<Question>> allQuestion() {
        return new ResponseEntity<>(repo.findAll(), HttpStatus.OK);
    }

    public List<Question> getQuestionByCategorie(String categorie) {
        return repo.findByCategorie(categorie);
    }

    public String addQuestion(Question question) {
        repo.save(question);
        return "success";
    }

    public ResponseEntity<List<Integer>> getQuestionForQuiz(String categorie, Integer numQuestions) {
        List<Integer> ids = repo.findRandomQuestionByCategorie(categorie, numQuestions);
        return new ResponseEntity<>(ids, HttpStatus.OK);
    }

    public ResponseEntity<List<QuestionWrapper>> getQuestionsFromId(List<Integer> questionIds) {
        List<QuestionWrapper> wrappers = new ArrayList<>();
        for (Integer id : questionIds) {
            repo.findById(id).ifPresent(q -> wrappers.add(new QuestionWrapper(
                    q.getId(), q.getQuestionTitle(),
                    q.getOption1(), q.getOption2(), q.getOption3(), q.getOption4())));
        }
        return new ResponseEntity<>(wrappers, HttpStatus.OK);
    }

    public ResponseEntity<Integer> getScore(List<Response> responses) {
        int right = 0;
        for (Response response : responses) {
            Optional<Question> question = repo.findById(response.getId());   // id = id de la QUESTION
            if (question.isPresent() && response.getResponse() != null
                    && response.getResponse().equals(question.get().getReponseCorrecte())) {
                right++;
            }
        }
        return new ResponseEntity<>(right, HttpStatus.OK);
    }
}
