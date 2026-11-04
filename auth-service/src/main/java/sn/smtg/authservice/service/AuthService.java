package sn.smtg.authservice.service;

import feign.FeignException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sn.smtg.authservice.dto.AuthRequest;
import sn.smtg.authservice.dto.AuthResponse;
import sn.smtg.authservice.dto.PasswordResetCompletionRequest;
import sn.smtg.authservice.dto.PasswordResetEmailRequest;
import sn.smtg.authservice.dto.PasswordResetRequest;
import sn.smtg.authservice.dto.PasswordResetTokenCompletionRequest;
import sn.smtg.authservice.dto.PasswordResetTokenRequest;
import sn.smtg.authservice.dto.UserWrapper;
import sn.smtg.authservice.feign.NotificationInterface;
import sn.smtg.authservice.feign.UserInterface;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final String RESET_REQUEST_MESSAGE =
            "Si un compte existe pour cette adresse, un lien de réinitialisation lui sera envoyé.";

    @Autowired
    UserInterface userInterface;

    @Autowired
    NotificationInterface notificationInterface;

    @Autowired
    JwtService jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Value("${user.service.internal-token:}")
    private String internalServiceToken;

    @Value("${app.public-url:http://localhost:8765}")
    private String publicAppUrl;

    public ResponseEntity<AuthResponse> login(AuthRequest request) {
        try {
            ResponseEntity<UserWrapper> userResponse =
                    userInterface.getUserByEmail(request.getEmail(), internalServiceToken);

            if (!userResponse.getStatusCode().is2xxSuccessful() || userResponse.getBody() == null) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            UserWrapper user = userResponse.getBody();

            if (!passwordEncoder.matches(request.getMotDePasse(), user.getMotDePasse())) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            String token = jwtService.genererToken(user.getId(), user.getEmail());
            return new ResponseEntity<>(new AuthResponse(token, user.getId(), user.getEmail()), HttpStatus.OK);

        } catch (feign.FeignException.NotFound e) {
            // e-mail inconnu : on ne distingue pas "inconnu" de "mauvais mot de passe"
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<Boolean> validerToken(String token) {
        return new ResponseEntity<>(jwtService.validerToken(token), HttpStatus.OK);
    }

    public ResponseEntity<String> demanderReinitialisation(PasswordResetRequest request) {
        if (request == null || request.email() == null || request.email().isBlank()
                || request.email().length() > 254) {
            return ResponseEntity.badRequest().body("Adresse e-mail requise.");
        }

        String email = request.email().trim();
        String etape = "recherche du compte";
        try {
            ResponseEntity<UserWrapper> userResponse =
                    userInterface.getUserByEmail(email, internalServiceToken);
            if (userResponse.getStatusCode().is2xxSuccessful() && userResponse.getBody() != null) {
                String token = genererJeton();
                String tokenHash = hacherJeton(token);
                LocalDateTime expiration = LocalDateTime.now().plusMinutes(30);
                etape = "enregistrement du jeton";
                userInterface.enregistrerJetonReinitialisation(
                        new PasswordResetTokenRequest(email, tokenHash, expiration), internalServiceToken);

                String resetUrl = publicAppUrl.replaceAll("/+$", "") + "/#resetToken=" + token;
                etape = "envoi du courriel";
                notificationInterface.envoyerLienReinitialisation(
                        new PasswordResetEmailRequest(email, resetUrl), internalServiceToken);
            }
            return ResponseEntity.ok(RESET_REQUEST_MESSAGE);
        } catch (FeignException.NotFound e) {
            return ResponseEntity.ok(RESET_REQUEST_MESSAGE);
        } catch (FeignException e) {
            logger.error("Échec du parcours de demande de réinitialisation pendant {} (HTTP {}).",
                    etape, e.status());
            if ("envoi du courriel".equals(etape)) {
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                        .body("Le lien n’a pas pu être envoyé pour le moment. Réessaie plus tard.");
            }
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Le service de réinitialisation est temporairement indisponible. Réessaie plus tard.");
        }
    }

    public ResponseEntity<String> reinitialiserMotDePasse(PasswordResetCompletionRequest request) {
        if (request == null || request.token() == null || request.token().isBlank()) {
            return ResponseEntity.badRequest().body("Le lien de réinitialisation est invalide ou expiré.");
        }
        if (!motDePasseValide(request.motDePasse())) {
            return ResponseEntity.badRequest()
                    .body("Le nouveau mot de passe doit contenir 8 à 72 octets en UTF-8.");
        }
        if (!request.token().matches("[A-Za-z0-9_-]{43}")) {
            return ResponseEntity.badRequest().body("Le lien de réinitialisation est invalide ou expiré.");
        }

        try {
            ResponseEntity<String> response = userInterface.terminerReinitialisation(
                    new PasswordResetTokenCompletionRequest(hacherJeton(request.token()), request.motDePasse()),
                    internalServiceToken);
            return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
        } catch (FeignException e) {
            if (e.status() == HttpStatus.BAD_REQUEST.value() || e.status() == HttpStatus.NOT_FOUND.value()) {
                return ResponseEntity.badRequest().body("Le lien de réinitialisation est invalide ou expiré.");
            }
            logger.error("Échec du parcours de réinitialisation (HTTP {}).", e.status());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Le service de réinitialisation est temporairement indisponible. Réessaie plus tard.");
        }
    }

    private String genererJeton() {
        byte[] aleatoire = new byte[32];
        SECURE_RANDOM.nextBytes(aleatoire);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(aleatoire);
    }

    private boolean motDePasseValide(String motDePasse) {
        return motDePasse != null && motDePasse.length() >= 8
                && motDePasse.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    private String hacherJeton(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponible pour sécuriser le jeton.", e);
        }
    }
}
