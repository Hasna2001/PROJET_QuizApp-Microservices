package sn.smtg.gratificationservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sn.smtg.gratificationservice.entity.Gratification;

import java.util.List;

@Repository
public interface GratificationRepo extends JpaRepository<Gratification, Integer> {

    List<Gratification> findByUtilisateurId(Integer utilisateurId);

    @Query("select coalesce(sum(g.points), 0) from Gratification g where g.utilisateurId = :utilisateurId")
    Integer sumPointsByUtilisateurId(@Param("utilisateurId") Integer utilisateurId);
}
