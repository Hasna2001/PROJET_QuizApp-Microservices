package sn.smtg.questionservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.smtg.questionservice.entity.Question;

import java.util.List;

@Repository
public interface QuestionRepo extends JpaRepository<Question, Integer> {

    List<Question> findByCategorie(String categorie);

    @Query(value = "select q.id from question q where q.categorie = :categorie " +
                   "order by rand() limit :numQuestions", nativeQuery = true)
    List<Integer> findRandomQuestionByCategorie(@Param("categorie") String categorie,
                                                @Param("numQuestions") int numQuestions);
}
