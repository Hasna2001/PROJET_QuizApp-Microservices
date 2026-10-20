package sn.smtg.quizservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sn.smtg.quizservice.entity.Quiz;

@Repository
public interface QuizRepo extends JpaRepository<Quiz, Integer> {
}
