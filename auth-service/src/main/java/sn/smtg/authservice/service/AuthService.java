package sn.smtg.authservice.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sn.smtg.authservice.dto.AuthRequest;
import sn.smtg.authservice.dto.AuthResponse;
import sn.smtg.authservice.dto.UserWrapper;
import sn.smtg.authservice.feign.UserInterface;

@Service
public class AuthService {

    @Autowired
    UserInterface userInterface;

    @Autowired
    JwtService jwtService;

    @Autowired
    PasswordEncoder passwordEncoder;

    public ResponseEntity<AuthResponse> login(AuthRequest request) {
        try {
            ResponseEntity<UserWrapper> userResponse = userInterface.getUserByEmail(request.getEmail());

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
}
