package sn.smtg.gratificationservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sn.smtg.gratificationservice.dto.GratificationDTO;
import sn.smtg.gratificationservice.dto.NotificationRequest;
import sn.smtg.gratificationservice.entity.Gratification;
import sn.smtg.gratificationservice.feign.NotificationInterface;
import sn.smtg.gratificationservice.feign.UserInterface;
import sn.smtg.gratificationservice.repository.GratificationRepo;

import java.util.List;
import java.util.Optional;

@Service
public class GratificationService {

    @Autowired
    GratificationRepo gratificationRepo;

    @Autowired
    UserInterface userInterface;

    @Autowired
    NotificationInterface notificationInterface;

    public ResponseEntity<String> ajouterGratification(GratificationDTO dto) {
        try {
            ResponseEntity<?> userResponse = userInterface.getUserById(dto.getUtilisateurId());
            if (!userResponse.getStatusCode().is2xxSuccessful()) {
                return new ResponseEntity<>("Utilisateur introuvable", HttpStatus.NOT_FOUND);
            }

            Gratification gratification = new Gratification();
            gratification.setUtilisateurId(dto.getUtilisateurId());
            gratification.setQuizId(dto.getQuizId());
            gratification.setScore(dto.getScore());
            Integer points = calculerPoints(dto.getScore());
            gratification.setPoints(points);
            gratificationRepo.save(gratification);

            try {
                notificationInterface.envoyerNotification(
                        new NotificationRequest(dto.getUtilisateurId(), dto.getQuizId(), dto.getScore(), points));
            } catch (Exception e) {
                System.err.println("Notification non envoyée : " + e.getMessage());
            }

            return new ResponseEntity<>("Gratification ajoutée avec succès", HttpStatus.CREATED);
        } catch (feign.FeignException.NotFound e) {
            return new ResponseEntity<>("Utilisateur introuvable", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur lors de l'ajout de la gratification", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Integer calculerPoints(Integer score) {
        if (score == null) return 10;
        if (score >= 10) return 100;
        if (score >= 7) return 70;
        if (score >= 5) return 50;
        if (score >= 3) return 30;
        return 10;
    }

    public ResponseEntity<List<Gratification>> getByUtilisateur(Integer utilisateurId) {
        return new ResponseEntity<>(gratificationRepo.findByUtilisateurId(utilisateurId), HttpStatus.OK);
    }

    public ResponseEntity<List<Gratification>> getAll() {
        return new ResponseEntity<>(gratificationRepo.findAll(), HttpStatus.OK);
    }

    public ResponseEntity<Gratification> getById(Integer id) {
        Optional<Gratification> g = gratificationRepo.findById(id);
        return g.map(x -> new ResponseEntity<>(x, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<Integer> totalPoints(Integer utilisateurId) {
        return new ResponseEntity<>(gratificationRepo.sumPointsByUtilisateurId(utilisateurId), HttpStatus.OK);
    }
}
