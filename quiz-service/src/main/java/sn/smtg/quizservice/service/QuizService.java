package sn.smtg.quizservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sn.smtg.quizservice.dto.GratificationDTO;
import sn.smtg.quizservice.dto.QuestionWrapper;
import sn.smtg.quizservice.dto.Response;
import sn.smtg.quizservice.entity.Quiz;
import sn.smtg.quizservice.feign.GratificationInterface;
import sn.smtg.quizservice.feign.QuizInterface;
import sn.smtg.quizservice.repository.QuizRepo;

import java.util.List;
import java.util.Optional;

@Service
public class QuizService {

    @Autowired
    QuizInterface quizInterface;

    @Autowired
    GratificationInterface gratificationInterface;

    @Autowired
    QuizRepo quizRepo;

    public ResponseEntity<String> createQuiz(String categorie, int numQuestions, String titre) {
        List<Integer> questions = quizInterface.getQuestionForQuiz(categorie, numQuestions).getBody();
        if (questions == null || questions.isEmpty()) {
            return new ResponseEntity<>("Aucune question pour la catégorie " + categorie, HttpStatus.NOT_FOUND);
        }
        Quiz quiz = new Quiz();
        quiz.setTitre(titre);
        quiz.setQuestionsIds(questions);
        Quiz quizCree = quizRepo.save(quiz);
        return new ResponseEntity<>(quizCree.getId().toString(), HttpStatus.CREATED);
    }

    public ResponseEntity<List<QuestionWrapper>> questionsQuiz(Integer id) {
        Optional<Quiz> quiz = quizRepo.findById(id);
        if (quiz.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return quizInterface.getQuestionsFromId(quiz.get().getQuestionsIds());
    }

    public ResponseEntity<Integer> calculerResultat(Integer id, Integer utilisateurId, List<Response> responses) {
        ResponseEntity<Integer> scoreResponse = quizInterface.getScore(responses);
        Integer score = scoreResponse.getBody();

        try {
            gratificationInterface.ajouterGratification(new GratificationDTO(utilisateurId, id, score));
        } catch (Exception e) {
            // Non bloquant (tolérance de panne) mais SYNCHRONE : le thread attend quand même la réponse
            System.err.println("Gratification non enregistrée : " + e.getMessage());
        }

        return new ResponseEntity<>(score, HttpStatus.OK);
    }
}
