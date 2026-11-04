package sn.smtg.notificationservice.service;

import feign.FeignException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sn.smtg.notificationservice.dto.NotificationRequest;
import sn.smtg.notificationservice.dto.UserWrapper;
import sn.smtg.notificationservice.feign.UserInterface;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Autowired
    UserInterface userInterface;

    @Autowired
    EmailService emailService;

    public ResponseEntity<String> envoyerNotification(NotificationRequest request) {
        try {
            ResponseEntity<UserWrapper> userResponse = userInterface.getUserById(request.getUtilisateurId());
            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                return new ResponseEntity<>("Utilisateur introuvable", HttpStatus.NOT_FOUND);
            }
            UserWrapper user = userResponse.getBody();

            emailService.envoyerResultatQuiz(user, request.getQuizId(), request.getScore(), request.getPoints());

            return new ResponseEntity<>("Notification envoyée à " + user.getEmail(), HttpStatus.OK);
        } catch (FeignException.NotFound e) {
            return new ResponseEntity<>("Utilisateur introuvable", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logger.error("Échec de l'envoi de la notification pour l'utilisateur {} et le quiz {}",
                    request.getUtilisateurId(), request.getQuizId(), e);
            return new ResponseEntity<>("Erreur lors de l'envoi de la notification : " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
